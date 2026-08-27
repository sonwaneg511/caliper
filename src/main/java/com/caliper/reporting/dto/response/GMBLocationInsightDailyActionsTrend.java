package com.caliper.reporting.dto.response;

import java.util.Date;

import lombok.Data;

@Data
public class GMBLocationInsightDailyActionsTrend {
	
	    private Date date;
	    private Long drivingDirectionCount;
	    private Long calls;
	    private Long websiteClicks;


}
