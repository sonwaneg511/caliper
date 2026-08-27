package com.caliper.review.dto.response;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalReviewResponse {

	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonProperty("created_time")
	public Date createdTime;
	
	@JsonProperty("no_of_reviews")
	public Long noOfReviews;
	
}
