package com.caliper.dashboard.dto.insights;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapSearchTrend {
    private String month;
    private long totalActions;
    private long totalViews;
}