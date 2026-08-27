package com.caliper.review.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewGraphResponse {

	@JsonProperty("review_breakdown")
	public ReviewBreakdown reviewBreakdown;
	
	@JsonProperty("total_review_graphs")
	public List<TotalReviewResponse> totalReviewResponse;
	
	@JsonProperty("starRating_count")
	public List<StarRatingCount> starRatingCount;
	
	@JsonProperty("pending_responses")
	public int pendingResponses;
	
	@JsonProperty("total_reviews")
	public int totalReviews;
	
	@JsonProperty("average_rating")
	public double averageRating;
	
}
