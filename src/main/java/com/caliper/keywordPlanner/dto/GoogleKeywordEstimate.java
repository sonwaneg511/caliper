package com.caliper.keywordPlanner.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleKeywordEstimate {

	private String phrase;
	private Long searchVolume;
	private String location;
	private List<GoogleMonthlySearches> monthlySearches;
}
