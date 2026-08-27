package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsolidateCampaignReport {

	@JsonProperty("total_cost")
	private double totalCost;
	@JsonProperty("total_planned_cost")
	private double totalPlannedCost;
	@JsonProperty("total_delivered_clicks")
	private double totalDeliveredClicks;
	@JsonProperty("total_video_views")
	private double totalVideoViews;
	@JsonProperty("total_cost_per_views")
	private double totalCostPerViews;
	@JsonProperty("ctr")
	private double ctr;
	@JsonProperty("cpm")
	private double cpm;
	@JsonProperty("vtr")
	private double vtr;
	
}