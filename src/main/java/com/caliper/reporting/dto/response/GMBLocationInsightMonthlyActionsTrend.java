package com.caliper.reporting.dto.response;

import lombok.Data;

@Data
public class GMBLocationInsightMonthlyActionsTrend {

	    private String month;        // e.g. "2024-08"
	    private Long drivingDirectionCount;
	    private Long calls;
	    private Long websiteClicks;
	
}
