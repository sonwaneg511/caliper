package com.caliper.campaign.google.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CampaignReportDaily {

	@JsonProperty("date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date date;
	@JsonProperty("cost")
	private double cost; //Clicks * CPC
	@JsonProperty("planned_cost")
	private double plannedCost; //Clicks * CPC
	@JsonProperty("delivered_impressions")
	private double deliveredImpressions; //ads_campaignbasicstats
	@JsonProperty("delivered_clicks")
	private double deliveredClicks; //ads_campaignbasicstats
	@JsonProperty("video_views")
	private double videoViews;
	@JsonProperty("conversions")
	private double conversions;
	@JsonProperty("ctr")
	private double ctr;
	@JsonProperty("vtr")
	private double vtr;
	@JsonProperty("cost_per_conversion")
	private double costPerConversion; //-- divide cost/conversion
	@JsonProperty("cpc")
	private double cpc; //-- divide cost/conversion
	@JsonProperty("cpm")
	private double cpm; //-- divide cost/conversion
}
