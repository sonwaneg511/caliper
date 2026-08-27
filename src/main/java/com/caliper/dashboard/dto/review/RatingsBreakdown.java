package com.caliper.dashboard.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RatingsBreakdown {

	private int fiveStar;
	private int fourStar;
	private int threeStar;
	private int twoStar;
	private int oneStar;
}
