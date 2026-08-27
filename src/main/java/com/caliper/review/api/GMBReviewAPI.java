package com.caliper.review.api;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.review.dto.response.GMBReviewResponse;
import com.caliper.review.entity.GMBReview;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusiness.v4.MyBusiness.Accounts.Locations;
import com.google.api.services.mybusiness.v4.model.BatchGetReviewsRequest;
import com.google.api.services.mybusiness.v4.model.BatchGetReviewsResponse;
import com.google.api.services.mybusiness.v4.model.LocationReview;
import com.google.api.services.mybusiness.v4.model.Review;
import com.google.api.services.mybusiness.v4.model.ReviewReply;

@Component
public class GMBReviewAPI {

	public GMBReviewResponse getAccountReviewResponse(String pageToken, MyBusiness business, Map<String, GMBLocation> locMap, List<String> gmbLocationIds, String accountId) throws Exception{
		
		List<GMBReview> gmbReviews = new ArrayList<GMBReview>();
		BatchGetReviewsResponse reviewsByAccount = getReviewsByAccount(pageToken, business, gmbLocationIds, accountId);
		List<LocationReview> locationReviews = reviewsByAccount.getLocationReviews();
		GMBReviewResponse gmbReviewResponse = null; 
		String repliedBy = "";

		if(locationReviews != null) {
			
			for(LocationReview locReview : locationReviews) {
				String gmbLocationName = locReview.getName();
				Review review = locReview.getReview();
				ZonedDateTime createZoneTime = ZonedDateTime.parse(review.getUpdateTime());
				Date createTime = Date.from(createZoneTime.toInstant());
				String updateTime = review.getUpdateTime();
				Instant updateInstantTime = Instant.parse(updateTime);				
				long starRating = getStarRating(review.getStarRating());
				String replyComment = "";
				
				String replyStatus = GMBReview.REPLY_STATUS_NO; 
				boolean reviewStatus = false;
				Date replyTime = null;
				
				if(review.getReviewReply() != null) {
					replyComment = review.getReviewReply().getComment();
					replyStatus = GMBReview.REPLY_STATUS_DEPLOYED;
					repliedBy   = GMBReview.REPLIED_BY_UNKNOWN;

					ZonedDateTime replyZoneTime = ZonedDateTime.parse(review.getReviewReply().getUpdateTime());
					replyTime = Date.from(replyZoneTime.toInstant());

					reviewStatus = true;
				}

				GMBLocation gmbLocation = locMap.get(gmbLocationName);
				GMBReview gmbReview = new GMBReview(review.getReviewId(), gmbLocation.getDealerId(), review.getName(), starRating, review.getComment(), replyComment, review.getReviewer().getDisplayName(),
						replyStatus, reviewStatus, createTime, replyTime, repliedBy, new Date(), updateInstantTime);
				gmbReviews.add(gmbReview);
			}
			String nextPageToken = reviewsByAccount.getNextPageToken();
			gmbReviewResponse = new GMBReviewResponse(nextPageToken, gmbReviews);
		}
		return gmbReviewResponse;
	}
	
	private long getStarRating(String starRating) {
		long starRatingValue = 0;

		if (starRating.equalsIgnoreCase(GMBReview.STAR_RATING_NOT_SPECIDIED)) {
			starRatingValue = 0;
		} else if (starRating.equalsIgnoreCase(GMBReview.STAR_RATING_ONE)) {
			starRatingValue = 1;
		} else if (starRating.equalsIgnoreCase(GMBReview.STAR_RATING_TWO)) {
			starRatingValue = 2;
		} else if (starRating.equalsIgnoreCase(GMBReview.STAR_RATING_THREE)) {
			starRatingValue = 3;
		} else if (starRating.equalsIgnoreCase(GMBReview.STAR_RATING_FOUR)) {
			starRatingValue = 4;
		} else if (starRating.equalsIgnoreCase(GMBReview.STAR_RATING_FIVE)) {
			starRatingValue = 5;
		}
		return starRatingValue;
	}
	
    private BatchGetReviewsResponse getReviewsByAccount(String pageToken, MyBusiness business, List<String> gmbLocationIds, String accountId) throws Exception {

		BatchGetReviewsResponse reviewsResponse = null;
		if(pageToken != null && pageToken != "" && !pageToken.isBlank()) {
			BatchGetReviewsRequest request = new BatchGetReviewsRequest();
			List<String> locationNames = new ArrayList<>();

			locationNames.addAll(gmbLocationIds);
			request.setLocationNames(locationNames);
			request.setPageToken(pageToken);
			request.setPageSize(50);
			request.setIgnoreRatingOnlyReviews(false);

			reviewsResponse = business.accounts().locations().batchGetReviews(accountId, request).execute();
		}else {
			BatchGetReviewsRequest request = new BatchGetReviewsRequest();
			List<String> locationNames = new ArrayList<>();
			
			locationNames.addAll(gmbLocationIds);
			request.setLocationNames(locationNames);
			request.setPageSize(50);
			request.setIgnoreRatingOnlyReviews(false);

			reviewsResponse = business.accounts().locations().batchGetReviews(accountId, request).execute();
		}
		return reviewsResponse;
	}
    
	public String setReviewReply(MyBusiness business, GMBReview gmbReview) throws SQLException, IOException {	
		Locations locations = business.accounts().locations();
		String reviewName = gmbReview.getReviewName();
		ReviewReply reply = new ReviewReply();
		reply.setComment(gmbReview.getReplyComment());
		reply = locations.reviews().updateReply(reviewName, reply).execute();

		return gmbReview.getReviewId();
	}
	
}
