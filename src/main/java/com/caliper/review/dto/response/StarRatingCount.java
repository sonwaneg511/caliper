package com.caliper.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarRatingCount {

	public int stars;
	
	public int percentage;
	
}
