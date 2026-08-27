package com.caliper.review.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewBreakdown {

	public int positive;
	
	public int neutral;
	
	public int negative;
	
}
