package com.caliper.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.bigquery.service.BigQueryConfig;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.repository.GMBLocationRepository;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.service.ClientService;
import com.caliper.review.api.GMBReviewAPI;
import com.caliper.review.dto.response.GMBReviewResponse;
import com.caliper.review.entity.DealerReviewAutoResponse;
import com.caliper.review.entity.GMBReview;
import com.caliper.review.entity.ReviewSettings;
import com.caliper.review.repository.DealerReviewAutoResponseRepository;
import com.caliper.review.repository.GMBReviewRepository;
import com.caliper.review.repository.ReviewSettingsRepository;
import com.caliper.review.service.ReviewService;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.cloud.bigquery.BigQuery;

@Service
public class GMBReviewDeployementTask extends ParameterizedJob {

	@Autowired
	public ReviewSettingsRepository reviewSettingsRepository;

	@Autowired
	public GMBReviewAPI reviewApi;

	@Autowired
	public ClientService clientService;

	@Autowired
	public ClientRepository clientRepository;

	@Autowired
	public GMBLocationRepository gmbLocationRepository;

	@Autowired
	public GMBReviewRepository gmbReviewRepository;

	@Autowired
	private GMBSessionFactory gmbSessionFactory;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private BigQueryConfig bigQueryConfig;

	@Autowired
	private DealerReviewAutoResponseRepository dealerReviewAutoResponseRepository;

	public List<Client> clientList;
	public boolean processAutoResponse;
	public boolean fetchReviewsAccount;
	public String gmbSettingsParamDate;
	public int reviewPageSize;
	public String fetchDealerIds;
	public MyBusiness business;
	public boolean insertBQ;
	private BigQuery bigQuery;
	private boolean deleteReply;
	public boolean processReviews;

	@Override
	public void run() {
		try {
			log("job started");
			init();
			processReview();
			fetchReviewsAccount();
			processAutoResponse();
			log("job end");
		} catch (Exception e) {
			log(e.getMessage());
		}
	}

	private void init() throws FileNotFoundException, IOException {
		this.processAutoResponse = parameters.getBoolean("process-auto-response");
		this.processReviews = parameters.getBoolean("process-review");
		this.fetchReviewsAccount = parameters.getBoolean("fetch-reviews-account", false);
		this.gmbSettingsParamDate = parameters.getString("gmb-settings-param-date","");
		this.deleteReply = parameters.getBoolean("delete-reply");
		this.insertBQ = parameters.getBoolean("insert-bq");
		this.reviewPageSize = parameters.getInt("review-page-size", 50);
		this.fetchDealerIds = parameters.getString("fetch-location-ids");
		String clientId = parameters.getString("client-id", null);
		if (clientId != null && !clientId.isBlank()) {
			this.clientList = List.of(clientService.findByClientId(clientId));
		} else {
			this.clientList = clientRepository.findAll();
		}
		System.out.println("no of clients : " + clientList.size());
		log("no of clients : " + clientList.size());
		// this.bigQuery = bigQueryConfig.getBigQueryDao();
	}

	private void fetchReviewsAccount() throws Exception {

		if (!fetchReviewsAccount) {
			return;
		}

		log("Fetching Reviews");
		System.out.println("Fetching Reviews");
		for(Client client : clientList) {
			log("processing for client : "+client.getClientName());
			fetchReviewsAccount(client);
		}
	}

	private void fetchReviewsAccount(Client client) throws Exception {

		this.business = gmbSessionFactory.getGMBSession(client.getClientId());
		System.out.println("client");
		Instant now = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
		String currentDate = now.atOffset(ZoneOffset.UTC).format(formatter);
		ReviewSettings reviewSettings = reviewSettingsRepository.findByClientIdAndPlatform(client.getClientId(),
				ReviewSettings.PLATFORM_GMB);
		String reviewSettingsLastInsertedDate = reviewSettings == null ? "" : reviewSettings.getReviewLastInsertedDate();
		String gmbSettingsDate = StringUtils.isNotEmpty(gmbSettingsParamDate) ? gmbSettingsParamDate
				: reviewSettingsLastInsertedDate;

		log("GMB setting data =" + gmbSettingsDate);

		// convert DB string → Instant
		Instant gmbSettingsParseDate = gmbSettingsDate.equalsIgnoreCase("") ? null : Instant.parse(gmbSettingsDate);

		log("GMB setting data after parsing =" + gmbSettingsParseDate);

		int reviewPageSizes = 50;
		if (reviewPageSize != 0) {
			reviewPageSizes = reviewPageSize;
		}

		// Then subtract 7 days to get correct data
		gmbSettingsParseDate = gmbSettingsParseDate == null ? null : gmbSettingsParseDate.minus(7, ChronoUnit.DAYS);

		log("GMB setting data after minus 7 days = " + gmbSettingsParseDate);

		boolean dealersSpecified = (fetchDealerIds != null && !fetchDealerIds.isBlank()) ? true : false;

		List<GMBLocation> gmbLocations = new ArrayList<>();
		if (dealersSpecified) {

			List<String> dealerIdsMentioned = Arrays.stream(fetchDealerIds.split(",")).map(String::trim)
					.filter(s -> !s.isEmpty()).collect(Collectors.toList());
			gmbLocations = gmbLocationRepository.findByClientIdAndDealerIdIn(client.getClientId(), dealerIdsMentioned);

		} else {
			gmbLocations = gmbLocationRepository.findByClientId(client.getClientId());
		}

		log("GMB locations size : " + gmbLocations.size());

		Map<String, List<GMBLocation>> accountLocationMap = gmbLocations.stream()
				.collect(Collectors.groupingBy(GMBLocation::getAccountId));
		Map<String, GMBLocation> locationMap = gmbLocations.stream()
				.collect(Collectors.toMap(GMBLocation::getGmbLocationId, l -> l));

		fetchReviewCallAccount(accountLocationMap, gmbSettingsParseDate, locationMap, reviewPageSizes,
				client.getClientId());

		reviewSettings.setReviewLastInsertedDate(currentDate);
		reviewSettingsRepository.save(reviewSettings);
	}

	private void fetchReviewCallAccount(Map<String, List<GMBLocation>> accountLocationMap, Instant gmbSettingsParseDate,
			Map<String, GMBLocation> locationMap, int reviewPageSize, String clientId) throws Exception {

		long count = 0;
		for (Map.Entry<String, List<GMBLocation>> entry : accountLocationMap.entrySet()) {

			Set<String> batch = new HashSet<String>();
			String accountId = entry.getKey();
			List<GMBLocation> gmbLocationsAccount = entry.getValue() != null ? entry.getValue()
					: new ArrayList<GMBLocation>();

			for (GMBLocation loc : gmbLocationsAccount) {
				log("review fetch for location : " + loc.getDealerId());
				if (loc.getStatus() != null && !loc.getStatus().equalsIgnoreCase("")) {
					if (loc.getStatus().equalsIgnoreCase(GMBLocation.LOCATION_STATE_VERIFIED)) {
						log("Processing norml");
						log("sending ids = " + loc.getDealerId());

						batch.add(loc.getGmbLocationId());
						count++;
						if ((count % reviewPageSize) == 0) {
							fetchBatchReviews(batch, accountId, gmbSettingsParseDate, locationMap, clientId);
							batch.clear();
						}
					}
				} else {
					log("Review is skiped for the dealer id :: " + loc.getDealerId()
							+ " , Due to status is null or empty");
				}
			}
			if (batch.size() > 0) {
				fetchBatchReviews(batch, accountId, gmbSettingsParseDate, locationMap, clientId);
			}
		}
	}

	private List<GMBReview> insertReviewsInSql(GMBReview review, String clientId) {
		List<GMBReview> filteredReviews = new ArrayList<GMBReview>();
		String replyComment = "";
		String repliedBy = "";
		String replyStatus = GMBReview.REPLY_STATUS_NO;
		boolean reviewStatus = false;
		Date createTime = review.getCreatedTime();
		if (review.getReplyComment() != null && review.getReplyComment() != "") {
			replyComment = review.getReplyComment();
			replyStatus = GMBReview.REPLY_STATUS_DEPLOYED;
			repliedBy = GMBReview.REPLIED_BY_UNKNOWN;
			reviewStatus = true;
		}

		boolean inserted = reviewService.insertGMBReview(clientId, review.getDealerId(), createTime,
				review.getReplyTime(), review.getStarRating(), review.getReviewId(),
				review.getReviewName(), review.getComment(), replyComment, review.getReviewer(),
				replyStatus, reviewStatus, repliedBy, review.getInsertedDate(),
				review.getInstantDate(), review.getEditedDate(), deleteReply, business);

		if (inserted) {
			GMBReview gmbReview = new GMBReview(0L, clientId, review.getDealerId(), createTime,
					review.getReplyTime(), review.getStarRating(), review.getReviewId(),
					review.getReviewName(), review.getComment(), replyComment, review.getReviewer(),
					replyStatus, reviewStatus, repliedBy, review.getInsertedDate(),
					review.getInstantDate(), review.getEditedDate());
			gmbReviewRepository.save(gmbReview);
			filteredReviews.add(gmbReview);
		}
	return filteredReviews;
	
	}
	
	private void fetchBatchReviews(Set<String> gmbLocationIds, String accountId, Instant gmbSettingsParseDate,
			Map<String, GMBLocation> locationMap, String clientId) throws Exception {
		log("Processing Location Ids - " + gmbLocationIds);
		boolean getNextPage = true;
		String pageToken = "";
		List<String> gmbLocationIdsList = gmbLocationIds.stream().collect(Collectors.toList());
		List<GMBReview> filteredReviews = new ArrayList<GMBReview>();
		while (getNextPage) {
			for (int i = 0; i < 4; i++) {
				try {
					Thread.sleep(5000);
					log("Attempt No - " + (i + 1) + " : Page Token - " + pageToken);

					// add logs for API call
					GMBReviewResponse gmbReviewResponse = reviewApi.getAccountReviewResponse(pageToken, business,
							locationMap, gmbLocationIdsList, accountId);

					if (gmbReviewResponse == null) {
						getNextPage = false;
						break;
					}
					List<GMBReview> gmbReviews = gmbReviewResponse.getReviews();
					if (gmbReviews == null) {
						getNextPage = false;
						break;
					}

					
					for (GMBReview review : gmbReviews) {
						Instant updateInstantTime = review.getInstantDate();
						log("updateInstantTime :: " + updateInstantTime);
						log("review name :: " + review.getReviewName() + " reviewer name :: " + review.getReviewer()
								+ " review id from api :: " + review.getReviewId() + " API Update time :: "
								+ updateInstantTime + " should be after gmb date :: " + gmbSettingsParseDate);
						if (gmbSettingsParseDate != null && updateInstantTime.isAfter(gmbSettingsParseDate)) {
							filteredReviews = insertReviewsInSql(review, clientId);
						}else if(gmbSettingsParseDate == null) {
							filteredReviews = insertReviewsInSql(review, clientId);
						
						}else {
							getNextPage = false;
							break;
						}

					}
					if (insertBQ) {
						reviewService.insertGMBReviewBQ(bigQuery, clientId, filteredReviews);
						filteredReviews.clear();
					}
					pageToken = gmbReviewResponse.getPageToken();
					if (pageToken == null) {
						getNextPage = false;
					}
					break;
				} catch (Exception ex) {
					if (i >= 3) {
						log("The reviews  call failed more than 4 times. Will exit the operations now.");
						getNextPage = false;
						throw new RuntimeException(
								"The reviews  call failed more than 4 times. Will exit the operations now.", ex);
					}
					ex.printStackTrace();
					log("Error fetching reviews. Will sleep for 30 secs.");
					Thread.sleep(30000);
				}
			}
		}
	}

	private Map<String, GMBLocation> getGMBLocationMap(String clientId) {
		List<GMBLocation> gmbLocations = gmbLocationRepository.findByClientId(clientId);
		Map<String, GMBLocation> gmbLocationMap = gmbLocations.stream()
				.filter(l -> l.getStatus().equalsIgnoreCase(GMBLocation.LOCATION_STATE_VERIFIED))
				.collect(Collectors.toMap(GMBLocation::getDealerId, l -> l));
		return gmbLocationMap;
	}

	private void processAutoResponse() {
		if (!processAutoResponse) {
			return;
		}
		log("Processing Auto Response");

		for (Client client : clientList) {

			ReviewSettings reviewSettings = reviewSettingsRepository.findByClientIdAndPlatform(client.getClientId(),
					ReviewSettings.PLATFORM_GMB);
			Date gmbSettingsParseDate = reviewSettings.getAutoResponseDate();
			log("Reviews after : " + gmbSettingsParseDate);

			ThreadLocalRandom tlr = ThreadLocalRandom.current();
			List<GMBReview> gmbReviews = gmbReviewRepository.findByClientIdAndReplyStatusAndReviewStatus(
					client.getClientId(), GMBReview.REPLY_STATUS_NO, false);
			int updated = 0;
			Map<String, GMBLocation> gmbLocationMap = getGMBLocationMap(client.getClientId());
			for (GMBReview gmbReview : gmbReviews) {
				if (gmbLocationMap.get(gmbReview.getDealerId()) != null) {

					String status = gmbLocationMap.get(gmbReview.getDealerId()).getStatus();
					if (status != null && status.equalsIgnoreCase(GMBLocation.LOCATION_STATE_DUPLICATE)) {
						log("Status Is Duplicate For Location Id :" + gmbReview.getDealerId());
					} else {
						if (!gmbReview.getCreatedTime().after(gmbSettingsParseDate)) {
							continue;
						}

						String reviewer = gmbReview.getReviewer();
						long rating = gmbReview.getStarRating();
						boolean withComment = false;

						if (gmbReview.getComment() != null) {
							if (gmbReview.getComment().trim().length() != 0) {
								withComment = true;
							}
						}

						List<DealerReviewAutoResponse> gmbReviewsTemplates = dealerReviewAutoResponseRepository
								.findByClientIdAndSourceAndRatingAndWithCommentOrderByRatingDesc(client.getClientId(),
										DealerLocation.GMB, rating, withComment);
						int max = (gmbReviewsTemplates == null) ? 0 : gmbReviewsTemplates.size();

						if (max != 0) {
							int randomNum = tlr.nextInt(0, max);
							DealerReviewAutoResponse gmbReviewTemplate = gmbReviewsTemplates.get(randomNum);
							String replyComment = gmbReviewTemplate.getReviewComment();

							GMBReview review = gmbReviewRepository.findByReviewIdAndClientId(gmbReview.getReviewId(),
									gmbReview.getClientId());
							review.setReplyComment(replyComment);
							review.setReviewer(reviewer);
							review.setRepliedBy(GMBReview.REPLIED_BY_AUTO);
							review.setReviewStatus(true);
							gmbReviewRepository.save(review);
							updated++;
						}

					}
				}
			}
			log("Auto response records updated - " + updated);

		}
	}

	private void processReview() {

		if (!processReviews) {
			return;
		}

		for (Client client : clientList) {
			try {
				this.business = gmbSessionFactory.getGMBSession(client.getClientId());
			} catch (Exception e) {
				log("Failed to get GMB session for client: " + client.getClientId() + " - " + e.getMessage());
				continue;
			}
			log("processing for client : " + client.getClientName());
			List<GMBReview> gmbReviews = gmbReviewRepository.findByClientIdAndReplyStatus(client.getClientId(),
					GMBReview.REPLY_STATUS_DRAFTED);
			Map<String, GMBLocation> gmbLocationMap = getGMBLocationMap(client.getClientId());

			for (GMBReview gmbReview : gmbReviews) {
				try {
					if (gmbLocationMap.get(gmbReview.getDealerId()) != null) {
						String status = gmbLocationMap.get(gmbReview.getDealerId()).getStatus();

						if (status != null && status.equalsIgnoreCase(GMBLocation.LOCATION_STATE_DUPLICATE)) {
							log("Status Is Duplicate For Dealer Id :" + gmbReview.getDealerId());
						} else {
							log("Deploying Review - " + gmbReview.getReviewId() + " : Dealer Id - "
									+ gmbReview.getDealerId() + " : Created Time - " + gmbReview.getCreatedTime());
							String reviewId = reviewApi.setReviewReply(business, gmbReview);
							GMBReview review = gmbReviewRepository.findByReviewIdAndClientId(reviewId,
									client.getClientId());
							review.setReplyStatus(GMBReview.REPLY_STATUS_DEPLOYED);
							gmbReviewRepository.save(review);
						}
					}
				} catch (Exception ex) {
					log("Error deploying review - " + gmbReview.getReviewId());
				}
			}
		}
	}

}
