package com.caliper.dashboard.dto.review;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonRootName("review")
public class DashboardReviews {

	private ReviewSummary reviewSummary;
	private ReviewSentiment reviewSentiment;
	private RatingsBreakdown ratingsBreakdown;
	private List<ReviewLocation> topReviewLocations;
	private List<ReviewLocation> lowestReviewLocations;
	private List<ReviewsChart>reviewsChart;
}
