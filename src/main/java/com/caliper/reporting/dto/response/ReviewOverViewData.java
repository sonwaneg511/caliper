package com.caliper.reporting.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewOverViewData {
	
	@JsonProperty("reviews_replied_after_month")
	public long ReviewsRepliedAfterMonth;
	
	@JsonProperty("avg_review")
	public double avgReview;

	@JsonProperty("unreplied_review")
	public double unrepliedReview;
}
