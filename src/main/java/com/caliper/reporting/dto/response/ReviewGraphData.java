package com.caliper.reporting.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewGraphData {

	@JsonProperty("date")
	private LocalDate date;

    @JsonProperty("totalReviews")
    private long totalReviews;

    @JsonProperty("avgRating")
    private double avgRating;
}