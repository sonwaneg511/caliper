package com.caliper.review.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.caliper.dashboard.dto.review.AggregatedReviewDTO;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.repository.FacebookLocationRepository;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.service.DealerLocationService;
import com.caliper.review.api.FacebookReviewAPI;
import com.caliper.review.api.GMBReviewAPI;
import com.caliper.review.dto.request.ReviewGraphDTO;
import com.caliper.review.dto.request.ReviewReplyRequest;
import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.review.dto.response.ReviewBreakdown;
import com.caliper.review.dto.response.ReviewGraphResponse;
import com.caliper.review.dto.response.ReviewPageResponse;
import com.caliper.review.dto.response.ReviewReplyResponse;
import com.caliper.review.dto.response.ReviewResponse;
import com.caliper.review.dto.response.StarRatingCount;
import com.caliper.review.dto.response.TotalReviewResponse;
import com.caliper.review.entity.FacebookReview;
import com.caliper.review.entity.GMBReview;
import com.caliper.review.repository.FacebookReviewRepository;
import com.caliper.review.repository.GMBReviewRepository;
import com.caliper.review.specification.FacebookReviewSpecification;
import com.caliper.review.specification.GMBReviewSpecification;
import com.caliper.usermanagement.entity.Roles;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ReviewNotFoundException;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusiness.v4.MyBusiness.Accounts.Locations;
import com.google.auth.oauth2.ClientId;

@Service
public class ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ReviewService.class);

	@Autowired
	private GMBReviewRepository gmbReviewRepository;

	@Autowired
	private FacebookReviewRepository facebookReviewRepository;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	private DealerLocationService dealerLocationService;
	
	@Autowired
	public GMBReviewAPI gmbReviewApi;
	
	@Autowired
	public FacebookReviewAPI facebookReviewApi;
	
	@Autowired
	private GMBSessionFactory gmbSessionFactory;
	
	@Autowired
	private FacebookLocationRepository facebookLocationRepository;

	//location request creation
	public LocationFilterRequest locationFilterRequestCreate(ReviewRequest req) {

		LocationFilterRequest locationRequest = LocationFilterRequest.builder()
				.clientId(req.getClientId())
				.userId(req.getUserId())
				.state(req.getState())
				.city(req.getCity())
				.dealerId(req.getDealerId())            
				.build();

		return locationRequest;
	}

	//filtered gmb review
	public Page<GMBReview> getFilteredGmbReviews(ReviewRequest req) {

		//--------------code review: use constant for createdTime-----------------------------
		Pageable pageable = PageRequest.of(req.getPageNo(), 10, Sort.by("createdTime").descending());

		LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		//---------------------use set------------------------------------------
		List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

		Page<GMBReview> reviewPage = gmbReviewRepository.findAll(GMBReviewSpecification.filterReviews(req, dealerIds), pageable);

		return reviewPage;	  
	}

	//filtered facebook review
	public Page<FacebookReview> getFilteredFacebookReviews(ReviewRequest req) {

		//--------------code review: use constant for createdTime-----------------------------
		Pageable pageable = PageRequest.of(req.getPageNo(), 10, Sort.by("createdTime").descending());

		LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

		Page<FacebookReview> reviewPage = facebookReviewRepository.findAll(FacebookReviewSpecification.filterReviews(req, dealerIds), pageable);
		return reviewPage;	  
	}

	//gmb review page response get
//	@PreAuthorize("@authorizationService.hasAccessToService(#req	.clientId,principal.username,T(com.caliper.usermanagement.entity.Roles).REVIEWS.name())" +
//			"&& @authorizationService.hasAccessToLocations(#req.clientId,principal.username, #req.dealerId)")
	@PreAuthorize("@authorizationService.hasAccessToService(#p0.clientId, authentication.name, 'REVIEWS')"
		 + " && @authorizationService.hasAccessToLocations(#p0.clientId, authentication.name, #p0.dealerId)")
        public ReviewPageResponse getReviewPageResponse(ReviewRequest req){

		List<ReviewResponse> reviewResponse = new ArrayList<>();
		LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		Map<String, DealerLocation> dealerIdVsLocationMap = locations.stream().collect(Collectors.toMap(l -> l.getDealerId(), l -> l));

		if(req.getPlatform().equalsIgnoreCase(DealerLocation.GMB)) {
			Page<GMBReview> gmbReviewList = getFilteredGmbReviews(req);
			
			for(GMBReview review : gmbReviewList) {

				//---------------code review: redundant data--------------put this in one method and bifurcate according to platform----------

				if(dealerIdVsLocationMap.containsKey(review.getDealerId())) {
					DealerLocation location = dealerIdVsLocationMap.get(review.getDealerId());

					ReviewResponse response = new ReviewResponse(review.getReviewer(), review.getStarRating(), review.getComment(), 
							review.getCreatedTime(), location.getDealerId(), location.getDealerName(), location.getAddress(), review.getReplyComment(), review.getReviewId(), review.getReplyStatus());

					reviewResponse.add(response);
				}
				//------------------------------code review----------------------------------------------------------

			}
			
			ReviewPageResponse reviewPageResponse = new ReviewPageResponse(reviewResponse, gmbReviewList.getTotalPages(), gmbReviewList.getTotalElements());
			return reviewPageResponse;
			
		}else if(req.getPlatform().equalsIgnoreCase(DealerLocation.FACEBOOK)) {
			Page<FacebookReview> facebookReviewList = getFilteredFacebookReviews(req);
		
			for(FacebookReview review : facebookReviewList) {

				//---------------code review: redundant data--------------put this in one method and bifurcate according to platform----------
				if(dealerIdVsLocationMap.containsKey(review.getDealerId())) {
					DealerLocation location = dealerIdVsLocationMap.get(review.getDealerId());

					ReviewResponse response = new ReviewResponse(review.getReviewer(), review.getStarRating(), review.getComment(), 
							review.getCreatedTime(), location.getDealerId(), location.getDealerName(), location.getAddress(), review.getReplyComment(), review.getReviewId(), review.getReplyStatus());

					reviewResponse.add(response);
				}
				//------------------------------code review----------------------------------------------------------
			}
			
			ReviewPageResponse reviewPageResponse = new ReviewPageResponse(reviewResponse, facebookReviewList.getTotalPages(), facebookReviewList.getTotalElements());
			return reviewPageResponse;
		}else {
			//------------------------code review: throw proper exception in the format of code,status and message--------------------or handle in GlobalExceptionHandler
			throw new InvalidRequestException("Invalid platform: " + req.getPlatform());
		}
		
	}

	//map response creation get
	public ReviewGraphResponse getReviewInsights(ReviewRequest req) {

		LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

		List<ReviewGraphDTO> filteredReviewList = new ArrayList<>();

		if(req.getPlatform().equalsIgnoreCase(DealerLocation.GMB)) {
			List<GMBReview> gmbReview = gmbReviewRepository.findAll(GMBReviewSpecification.filterReviews(req, dealerIds));
			filteredReviewList.addAll(convertGmb(gmbReview));
		}
		else if(req.getPlatform().equalsIgnoreCase(DealerLocation.FACEBOOK)) {
			List<FacebookReview> facebookReview = facebookReviewRepository.findAll(FacebookReviewSpecification.filterReviews(req, dealerIds));
			filteredReviewList.addAll(convertFb(facebookReview));
		}

		int positive =  (int)filteredReviewList.stream().filter(r -> r.getStarRating() == 4 || r.getStarRating() == 5).count();
		int neutral = (int)filteredReviewList.stream().filter(r -> r.getStarRating() == 3).count();
		int negative = (int)filteredReviewList.stream().filter(r -> r.getStarRating() == 2 || r.getStarRating() == 1).count();

		ReviewBreakdown reviewBreakDown = new ReviewBreakdown(positive, neutral, negative);

		//-------code review: instead of using multiple collections add the count directly in tree map and then collect in list0---------------------
		//current review list
		Map<Date, Long> currentReviewCountMap =
		        filteredReviewList.stream()
		                .collect(Collectors.groupingBy(r -> {
		                   return DateUtils.truncate(r.getCreatedTime(), Calendar.DATE);
		                }, Collectors.counting()));
		
		//sort the map by date
		Map<Date, Long> sortedReviewCountMap = new TreeMap<>(currentReviewCountMap);
		List<TotalReviewResponse> current = sortedReviewCountMap.entrySet().stream().map(e -> new TotalReviewResponse(e.getKey(), e.getValue())).collect(Collectors.toList());
//=-------------------------------code review-------------------------------------------------------------------------------------------------------
		//star rating
		List<StarRatingCount> starRatingCounts = IntStream.rangeClosed(1, 5).mapToObj(i -> new StarRatingCount(i, 
				(int) filteredReviewList.stream().filter(r -> r.getStarRating() == i).count())).collect(Collectors.toList());

		double averageRating = filteredReviewList.stream()
		        .mapToLong(ReviewGraphDTO::getStarRating)
		        .average()
		        .orElse(0.0);
		
		averageRating = Math.round(averageRating * 10.0) / 10.0;
		//pending responses
		int pendingResponses = (int)filteredReviewList.stream().filter(r -> r.getReplyStatus().equalsIgnoreCase(ReviewRequest.REPLY_STATUS_NO)).count();

		//total reviews
		int totalReviews = filteredReviewList.size();


		ReviewGraphResponse reviewGraphResponse = new ReviewGraphResponse(reviewBreakDown, current, starRatingCounts, pendingResponses, totalReviews, averageRating);
		return reviewGraphResponse;
	}

	private List<ReviewGraphDTO> convertGmb(List<GMBReview> reviews) {
		return reviews.stream()
				.map(r -> new ReviewGraphDTO(r.getStarRating(), r.getCreatedTime(), r.getReplyStatus()))
				.collect(Collectors.toList());
	}

	private List<ReviewGraphDTO> convertFb(List<FacebookReview> reviews) {
		return reviews.stream()
				.map(r -> new ReviewGraphDTO(r.getStarRating(), r.getCreatedTime(), r.getReplyStatus()))
				.collect(Collectors.toList());
	}

	//reply post method for gmb and facebook
	public ReviewReplyResponse postReviewReply(ReviewReplyRequest req) throws FileNotFoundException, IOException, SQLException {

		if(req.getPlatform().equalsIgnoreCase(DealerLocation.GMB)){
			GMBReview byReviewId = gmbReviewRepository.findByReviewIdAndClientId(req.getReviewId(), req.getClientId());
			if (byReviewId == null) {
				throw new ReviewNotFoundException("GMB Review not found: " + req.getReviewId());
			}

			byReviewId.setReplyComment(req.getReplyComment());
			byReviewId.setReplyStatus(ReviewRequest.REPLY_STATUS_DRAFTED);
			byReviewId.setReplyTime(new Date());
			byReviewId.setRepliedBy(req.getUserId());
			gmbReviewRepository.save(byReviewId);
			
			/*
			//code for deploy review reply
			MyBusiness business = gmbSessionFactory.getGMBSession();
			String reviewId = gmbReviewApi.setReviewReply(business, byReviewId);
			
			if(reviewId != null) {
				byReviewId.setReplyStatus(ReviewRequest.REPLY_STATUS_DEPLOYED);
				gmbReviewRepository.save(byReviewId);
			}*/
			
		}else if(req.getPlatform().equalsIgnoreCase(DealerLocation.FACEBOOK)) {
			FacebookReview byReviewId = facebookReviewRepository.findByReviewIdAndClientId(req.getReviewId(), req.getClientId());
			if (byReviewId == null) {
				throw new ReviewNotFoundException("Facebook Review not found: " + req.getReviewId());
			}

			byReviewId.setReplyComment(req.getReplyComment());
			byReviewId.setReplyStatus(ReviewRequest.REPLY_STATUS_DRAFTED);
			byReviewId.setReplyTime(new Date());
			byReviewId.setRepliedBy(req.getUserId());
			facebookReviewRepository.save(byReviewId);
			
			/*
			//code for deploy review reply
			FacebookLocation location = facebookLocationRepository.findByClientIdAndDealerId(req.getClientId(), byReviewId.getDealerId());
			String reviewId = facebookReviewApi.setReviewReply(byReviewId, location);
			
			if(reviewId != null) {
				byReviewId.setReplyStatus(ReviewRequest.REPLY_STATUS_DEPLOYED);
				facebookReviewRepository.save(byReviewId);
			}*/
			
		} else {
			throw new InvalidRequestException("Invalid platform: " + req.getPlatform());
		}


		return new ReviewReplyResponse(req.getUserId(), req.getReviewId(), req.getReplyComment(), ReviewRequest.SUCCESS);
	}

	public List<GMBReview> findByClientIdAndFromDate(String clientId, Set<String>dealerIds, Date fromDate, Date toDate){
			return gmbReviewRepository.findByClientIdAndDealerIdInAndCreatedTimeBetween(clientId, dealerIds, fromDate, toDate);
	}

	public List<AggregatedReviewDTO> fetchAggregatedReviews(String clientId, Set<String> mappedDealers, Date fromDate,
			Date toDate) {
		return gmbReviewRepository.fetchAggregatedReviews(clientId, mappedDealers, fromDate, toDate);
	}

	public boolean insertGMBReview(String clientId, String dealerId, Date createTime, Date replyTime, long starRating,
			String reviewId, String reviewName, String comment, String replyComment, String reviewer,
			String replyStatus, boolean reviewStatus, String repliedBy, Date insertedDate, Instant instantDate,
			Date editedDate, boolean deleteReply, MyBusiness business) {
		System.out.println("");
		GMBReview byReviewIdAndClientId = gmbReviewRepository.findByReviewIdAndClientId(reviewId, clientId);
		
		if(byReviewIdAndClientId == null) {
			return true;
		}else if (deleteReply && byReviewIdAndClientId.getCreatedTime().before(insertedDate)) {
			
			System.out.println("Edited Review - "+byReviewIdAndClientId.getReviewId());
			if(StringUtils.isNotBlank(byReviewIdAndClientId.getComment())) {
				
				if((StringUtils.isEmpty(byReviewIdAndClientId.getComment()) && !StringUtils.isEmpty(comment)) || 
						(StringUtils.isEmpty(comment) && !StringUtils.isEmpty(byReviewIdAndClientId.getComment())) || 
						(!StringUtils.isEmpty(byReviewIdAndClientId.getComment()) && !byReviewIdAndClientId.getComment().equalsIgnoreCase(comment)) || 
						byReviewIdAndClientId.getStarRating() != starRating) {
					
					byReviewIdAndClientId.setReplyStatus(GMBReview.REPLY_STATUS_NO);
					byReviewIdAndClientId.setReviewStatus(false);
					byReviewIdAndClientId.setCreatedTime(createTime);
					byReviewIdAndClientId.setStarRating(starRating);
					byReviewIdAndClientId.setComment(replyComment);// this should be comment
					byReviewIdAndClientId.setReplyComment("");
					byReviewIdAndClientId.setRepliedBy(repliedBy);
					gmbReviewRepository.save(byReviewIdAndClientId);
					
					//logic flawed.........it will always be no reply...as it is already set above
					if(!byReviewIdAndClientId.getReplyStatus().equalsIgnoreCase(GMBReview.REPLY_STATUS_NO) ) {
						System.out.println("Deleting reply for edited review - "+byReviewIdAndClientId.getReviewId());
						deleteReply(clientId, business, byReviewIdAndClientId);
					} else {
						System.out.println("Reply is edited for the review but not deleting the reply - "+byReviewIdAndClientId.getReviewId());
					}
				}
			}
		}
	    return false;
	}

	
	private void deleteReply(String clientId, MyBusiness business, GMBReview gmbReview) {
		try {
			Locations locations = business.accounts().locations();
			String reviewName = gmbReview.getReviewName();
			locations.reviews().deleteReply(reviewName).execute();
		} catch (Exception ex) {
			LOG.error("Failed to delete GMB review reply for reviewId={}: {}", gmbReview.getReviewId(), ex.getMessage(), ex);
			throw new RuntimeException("Failed to delete GMB review reply for reviewId: " + gmbReview.getReviewId(), ex);
		}
	}

	public void insertGMBReviewBQ(Object bigQuery, String clientId, List<GMBReview> filteredReviews) {

		
		
		
	}

	public boolean insertReview(String clientId, String dealerId, Date createTime, Date replyTime, long starRating,
			String reviewId, String comment, String replyComment, String reviewer, String replyStatus,
			boolean reviewStatus, String repliedBy, Date date, String facebookPageId) {
		FacebookReview byReviewId = facebookReviewRepository.findByReviewIdAndClientId(reviewId, clientId);
		if(byReviewId == null) {
			return true;
		}
		return false;
	}
}
