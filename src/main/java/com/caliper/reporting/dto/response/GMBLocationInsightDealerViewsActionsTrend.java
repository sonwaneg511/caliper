package com.caliper.reporting.dto.response;

import lombok.Data;

@Data
public class GMBLocationInsightDealerViewsActionsTrend {

	private String dealerId;
	private String dealerName;
	private String state;
	private String city;
	private Long viewsCount;
	private Long drivingDirectionCount;
    private Long calls;
    private Long websiteClicks;
}
