package com.caliper.review.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.review.dto.response.FacebookReviewResponse;
import com.caliper.review.entity.FacebookReview;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.OpenGraphRating;
import com.restfb.types.PageRating;
import com.restfb.types.RecommendationType;

@Component
public class FacebookReviewAPI {

	public FacebookReviewResponse getReviewResponse(String pageToken, FacebookLocation facebookLocation, String dealerId) throws Exception{
		List<FacebookReview> facebookReviews = new ArrayList<FacebookReview>();
		Connection<OpenGraphRating> reviews = getReviews(pageToken, facebookLocation);
		FacebookReviewResponse facebookReviewResponse = null;
		String repliedBy = "";
		if (reviews != null) {
			for (OpenGraphRating review : reviews.getData()) {
				Date createdTime = review.getCreatedTime();
				String starRating = review.getRecommendationType().equals(RecommendationType.POSITIVE) ? FacebookReview.REVIEW_RATING_MAX : FacebookReview.REVIEW_RATING_MIN;
				String replyComment = "";
				String replyStatus = FacebookReview.REPLY_STATUS_NO;
				boolean reviewStatus = false;
				Date replyTime = null;
				PageRating pageRating = review.getOpenGraphStory();
				String reviewer = review.getReviewer() == null ? "" : review.getReviewer().getName();
				
				FacebookReview facebookReview = new FacebookReview(pageRating.getId(), facebookLocation.getFacebookPageId(), dealerId,
						Long.parseLong(starRating), review.getReviewText(), replyComment, reviewer, replyStatus, reviewStatus,
						createdTime, replyTime, repliedBy, new Date());
				facebookReviews.add(facebookReview);
			}

			String nextPageToken = reviews.getAfterCursor();
			facebookReviewResponse = new FacebookReviewResponse(nextPageToken, facebookReviews);
		}

		return facebookReviewResponse;
	}
	
	private Connection<OpenGraphRating> getReviews(String pageToken, FacebookLocation facebookLocation) throws Exception {
		Connection<OpenGraphRating> pageReviews = null;
		FacebookClient fbClient = new DefaultFacebookClient(facebookLocation.getAccessToken(), Version.LATEST);
		if (pageToken != null && pageToken != "" && !pageToken.isBlank()) {
			pageReviews = fbClient.fetchConnection(facebookLocation.getFacebookPageId()+"/ratings", OpenGraphRating.class,
					Parameter.with("fields","id,created_time,has_rating,has_review,rating,recommendation_type,review_text,open_graph_story,reviewer"),
					Parameter.with("after", pageToken), Parameter.with("limit", "500"));
		}
		else {
			pageReviews = fbClient.fetchConnection(facebookLocation.getFacebookPageId()+"/ratings", OpenGraphRating.class,
					Parameter.with("fields","id,created_time,has_rating,has_review,rating,recommendation_type,review_text,open_graph_story,reviewer"), 
					Parameter.with("limit", "500"));
		}
		return pageReviews;
	}

	public String setReviewReply(FacebookReview facebookReview, FacebookLocation facebookLocation) {
		FacebookClient fbClient = new DefaultFacebookClient(facebookLocation.getAccessToken(), Version.LATEST);
		OpenGraphRating pageReviews = fbClient.publish(facebookReview.getReviewId()+"/comments", OpenGraphRating.class,
				Parameter.with("fields","id"),
				Parameter.with("message", facebookReview.getReplyComment()));
		return facebookReview.getReviewId();
	}
}
