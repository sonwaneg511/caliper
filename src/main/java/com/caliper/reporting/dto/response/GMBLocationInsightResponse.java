package com.caliper.reporting.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class GMBLocationInsightResponse {

	private List<GMBLocationInsightMonthlyViews> monthlyViews;
    private List<GMBLocationInsightMonthlyActionsTrend> monthlyActionsTrend;
    private List<GMBLocationInsightDailyViewsTrend> dailyViewsTrend;
    private List<GMBLocationInsightDailyActionsTrend> dailyActionsTrend;
    private List<GMBLocationInsightDealerViewsActionsTrend> dealerViewsActionsTrend;
    
}
