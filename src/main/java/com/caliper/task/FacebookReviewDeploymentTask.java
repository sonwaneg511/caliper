package com.caliper.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.job.runtime.ExecutableJob;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.repository.FacebookLocationRepository;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.service.ClientService;
import com.caliper.review.api.FacebookReviewAPI;
import com.caliper.review.dto.response.FacebookReviewResponse;
import com.caliper.review.entity.DealerReviewAutoResponse;
import com.caliper.review.entity.FacebookReview;
import com.caliper.review.entity.GMBReview;
import com.caliper.review.entity.ReviewSettings;
import com.caliper.review.repository.DealerReviewAutoResponseRepository;
import com.caliper.review.repository.FacebookReviewRepository;
import com.caliper.review.repository.ReviewSettingsRepository;
import com.caliper.review.service.ReviewService;
import com.google.api.services.mybusiness.v4.MyBusiness;

@Service
public class FacebookReviewDeploymentTask extends ParameterizedJob{

	@Autowired
	public FacebookReviewAPI reviewApi;

	@Autowired
	public FacebookReviewRepository facebookReviewRepository;

	@Autowired
	public FacebookLocationRepository facebookLocationRepository;

	@Autowired
	public ReviewSettingsRepository reviewSettingsRepository;

	@Autowired
	public ClientService clientService;

	@Autowired
	public ClientRepository clientRepository;

	@Autowired
	public ReviewService reviewService;

	@Autowired
	private DealerReviewAutoResponseRepository dealerReviewAutoResponseRepository;

	public List<Client> clientList;
	public boolean processAutoResponse;
	public boolean processReviews;
	public boolean fetchReviews;
	public String gmbSettingsParamDate;
	public int reviewPageSize;
	public String fetchLocationIds;

	@Override
	public void run() {

		try {
			init();
			fetchReviews();
			processReview();
			processAutoResponse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws FileNotFoundException, IOException {
		this.processAutoResponse = parameters.getBoolean("process-auto-response");
		this.processReviews = parameters.getBoolean("process-review");
		this.fetchReviews = parameters.getBoolean("fetch-reviews");
		this.gmbSettingsParamDate = parameters.getString("gmb-settings-param-date");
		this.reviewPageSize = parameters.getInt("review-page-size",50);
		this.fetchLocationIds = parameters.getString("fetch-location-ids");
		this.clientList = clientRepository.findAll();
	}

	private void fetchReviews() throws Exception {
		if(!fetchReviews) {
			return;
		}

		log("Fetching Reviews");

		for(Client client : clientList) {
			log("processing for client : "+client.getClientName());
			fetchReviews(client);
		}
	}

	private void fetchReviews(Client client) throws Exception {

		log("Fetching Reviews");

		ZonedDateTime currentDate = ZonedDateTime.now();
		ReviewSettings reviewSettings = reviewSettingsRepository.findByClientIdAndPlatform(client.getClientId(), ReviewSettings.PLATFORM_FACEBOOK);
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(reviewSettings.getReviewLastInsertedDate());
		Date facebookSettingsParseDate = Date.from(zonedDateTime.toInstant());

		boolean locationsSpecified = (fetchLocationIds != null && !fetchLocationIds.isBlank()) ? true : false;

		if(locationsSpecified) {
			List<String> locationIds = Arrays.stream(fetchLocationIds.split(",")).map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toList());
			List<FacebookLocation> facebookLocations = facebookLocationRepository.findByClientIdAndDealerIdIn(client.getClientId(), locationIds);
			fetchReviewCall(facebookLocations, facebookSettingsParseDate, client.getClientId());
		}else {
			List<FacebookLocation> facebookLocations = facebookLocationRepository.findByClientId(client.getClientId());
			fetchReviewCall(facebookLocations, facebookSettingsParseDate, client.getClientId());
		}

		reviewSettings.setReviewLastInsertedDate(String.valueOf(currentDate));
		reviewSettingsRepository.save(reviewSettings);
	}

	private void fetchReviewCall(List<FacebookLocation> facebookLocations, Date facebookSettingsParseDate, String clientId) throws Exception {
		for(FacebookLocation facebookLocation : facebookLocations) {
			Thread.sleep(1000);
			fetchReviews(facebookLocation, facebookSettingsParseDate, clientId);
		}
	}

	private void fetchReviews(FacebookLocation facebookLocation, Date facebookSettingsParseDate, String clientId) throws Exception {
		log("Fetching Reviews for location - " + facebookLocation.getFacebookPageId() + " : Date - " + facebookSettingsParseDate);
		boolean getNextPage = true;
		String pageToken = "";
		while (getNextPage) {
			log("Page Token=" + pageToken);
			Thread.sleep(200);
			FacebookReviewResponse facebookReviewResponse = reviewApi.getReviewResponse(pageToken, facebookLocation, facebookLocation.getDealerId());
			if (facebookReviewResponse == null) {
				break;
			}
			List<FacebookReview> facebookReviews = facebookReviewResponse.getReviews();
			if (facebookReviews == null) {
				break;
			}
			List<FacebookReview> filteredReviews = new ArrayList<FacebookReview>();
			for (FacebookReview review : facebookReviews) {
				Date createTime = review.getCreatedTime();
				if (createTime.after(facebookSettingsParseDate)) {
					String replyComment = "";
					String repliedBy = "";
					String replyStatus = FacebookReview.REPLY_STATUS_NO;
					boolean reviewStatus = false;

					if (review.getReplyComment() != null && review.getReplyComment() != "") {
						replyComment = review.getReplyComment();
						replyStatus = FacebookReview.REPLY_STATUS_DEPLOYED;
						repliedBy = FacebookReview.REPLIED_BY_UNKNOWN;
						reviewStatus = true;
					}

					boolean inserted = reviewService.insertReview(clientId, facebookLocation.getDealerId(),  createTime, review.getReplyTime(),  review.getStarRating(), review.getReviewId(),
							review.getComment(), replyComment, review.getReviewer(), replyStatus, reviewStatus, repliedBy,   new Date(), facebookLocation.getFacebookPageId());

					if(inserted) {
						FacebookReview facebookReview = new FacebookReview(0L, clientId, facebookLocation.getDealerId(),  createTime, review.getReplyTime(),  review.getStarRating(), review.getReviewId(),
								review.getComment(), replyComment, review.getReviewer(), replyStatus, reviewStatus, repliedBy,   new Date(), facebookLocation.getFacebookPageId());
						facebookReviewRepository.save(facebookReview);
						filteredReviews.add(facebookReview);
					} else {
						getNextPage = false;
						break;
					}
				} else {
					getNextPage = false;
					break;
				}
			}
			pageToken = facebookReviewResponse.getPageToken();
			log("Page Token set =" + pageToken);
			if (pageToken == null) {
				getNextPage = false;

			}
		}
	}

	private Map<String, FacebookLocation> getFacebookLocationMap(String clientId){
		List<FacebookLocation> facebookLocList = facebookLocationRepository.findByClientId(clientId);
		Map<String, FacebookLocation> facebookLocationMap = facebookLocList.stream().collect(Collectors.toMap(FacebookLocation::getDealerId, Function.identity()));
		return facebookLocationMap;
	}

	private void processAutoResponse() {

		if(!processAutoResponse) {
			return;
		}
		log("Processing Auto Response");

		for(Client client : clientList) {

			ReviewSettings reviewSettings = reviewSettingsRepository.findByClientIdAndPlatform(client.getClientId(), ReviewSettings.PLATFORM_FACEBOOK);

			Date fbSettingsParseDate = reviewSettings.getAutoResponseDate();
			log("Reviews after : "+fbSettingsParseDate);

			ThreadLocalRandom tlr = ThreadLocalRandom.current();
			List<FacebookReview> fbReviews = facebookReviewRepository.findByClientIdAndReplyStatus(client.getClientId(), FacebookReview.REPLY_STATUS_NO);
			int updated = 0;
			Map<String, FacebookLocation> map = getFacebookLocationMap(client.getClientId());

			for(FacebookReview fbReview : fbReviews) {
				if (map.get(fbReview.getDealerId()) != null) {
					boolean isPublished = map.get(fbReview.getDealerId()).isPublished();
					if (!isPublished) {
						log("Page is not published for Dealer Id - "+ fbReview.getDealerId());
					} else {
						if(!fbReview.getCreatedTime().after(fbSettingsParseDate)) {
							continue;
						}

						String reviewer = fbReview.getReviewer();
						long rating = fbReview.getStarRating();
						boolean withComment = false;
						String dealerId = fbReview.getDealerId();

						if(fbReview.getComment() != null) {
							if(fbReview.getComment().trim().length() != 0) {
								withComment = true;
							}
						}
						List<DealerReviewAutoResponse> fbReviewsTemplates = dealerReviewAutoResponseRepository.findByClientIdAndSourceAndRatingAndWithCommentOrderByRatingDesc(client.getClientId(), DealerLocation.FACEBOOK, rating, withComment);
						int max = (fbReviewsTemplates == null) ? 0 : fbReviewsTemplates.size();
						if(max != 0) {
							int randomNum = tlr.nextInt(0, max);

							DealerReviewAutoResponse gmbReviewTemplate =fbReviewsTemplates.get(randomNum);
							String replyComment = gmbReviewTemplate.getReviewComment();

							fbReview.setReplyComment(replyComment);
							fbReview.setReviewer(reviewer);
							fbReview.setRepliedBy(GMBReview.REPLIED_BY_AUTO);
							fbReview.setReviewStatus(true);
							facebookReviewRepository.save(fbReview);
							updated++;
						}
					}
				}
			}
			log("Auto response records updated - " + updated);
		}
	}

	private void processReview() {
		if(!processReviews) {
			return;
		}

		for(Client client : clientList) {

			List<FacebookReview> facebookReviews = facebookReviewRepository.findByClientIdAndReplyStatus(client.getClientId(), FacebookReview.REPLY_STATUS_DRAFTED);
			Map<String, FacebookLocation> facebookLocationMap = getFacebookLocationMap(client.getClientId());

			for (FacebookReview facebookReview : facebookReviews) {
				try {
					if(facebookLocationMap.get(facebookReview.getDealerId()) != null){
						FacebookLocation facebookLocation = facebookLocationMap.get(facebookReview.getDealerId());
						log("Deploying Review - " + facebookReview.getReviewId() + " : Dealer Id - " + facebookReview.getDealerId());
						String reviewId = reviewApi.setReviewReply(facebookReview, facebookLocation);
						facebookReview.setReplyStatus(FacebookReview.REPLY_STATUS_DEPLOYED);
						facebookReviewRepository.save(facebookReview);
					}
				} catch (Exception ex) {
					log("Error deploying review - " + facebookReview.getReviewId());
				}
			}
		}
	}






}
