package com.caliper.reporting.dto.response;

import lombok.Data;

@Data
public class GMBInsightLocationSumDataResponse {

	public double totalViews;
    public double desktopTotalViews;
    public double mobileTotalViews;
    public double totalMapViews;
    public double desktopMapViews;
    public double mobileMapViews;
    public double totalSearchViews;
    public double desktopSearchViews;
    public double mobileSearchViews;
    public double totalActions;
    public double totalDrivingDirectionActions;
    public double totalCallActions;
    public double totalWebsiteActions;
    
}
