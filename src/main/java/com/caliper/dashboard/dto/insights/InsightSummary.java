package com.caliper.dashboard.dto.insights;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("summary")
public class InsightSummary {
	private long totalViews;
    private long totalSearches;
    private long totalMapViews;
    private long callsInitiated;
    private long websiteClicks;
    private long drivingDirectionReq;
}