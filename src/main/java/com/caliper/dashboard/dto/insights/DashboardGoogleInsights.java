package com.caliper.dashboard.dto.insights;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("googleBusinessInsights")
public class DashboardGoogleInsights {

	private InsightSummary summary;
	private List<MapSearchTrend> mapSearchTrends;

}
