package com.caliper.campaign.google.dto.response;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportCampaignDailyReport {

	@JsonProperty("client_name")
	private String clientName;
	@JsonProperty("campaign_name")
	private String campaignName;
	@JsonProperty("report_date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date reportDate;
	@JsonProperty("planned_impressions")
	private double plannedImpressions;
	@JsonProperty("delivered_impressions")
	private double deliveredImpressions; //ads_campaignbasicstats
	@JsonProperty("planned_clicks")
	private double plannedClicks;
	@JsonProperty("delivered_clicks")
	private double deliveredClicks; //ads_campaignbasicstats
	@JsonProperty("average_cpc")
	private double averageCpc;// internalTable added as cpc
	@JsonProperty("delivered_avg_cpc")
	private double deliveredAvgCpc;
	@JsonProperty("cost")
	private double cost; //Clicks * CPC
	@JsonProperty("conversion")
	private double conversion; //Clicks * CPC
	@JsonProperty("ctr")
	private double ctr; //Clicks * CPC
	@JsonProperty("cost_per_conversion")
	private double costPerConversion;
}