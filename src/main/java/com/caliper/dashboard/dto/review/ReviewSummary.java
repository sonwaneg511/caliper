package com.caliper.dashboard.dto.review;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonRootName("summary")
public class ReviewSummary {

	private Long totalReviews;
	private Double avRating;
	private Double nps;
}
