package com.caliper.keywordPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleMonthlySearches {

	private String month;
	private String year;
	private Long count;
}
