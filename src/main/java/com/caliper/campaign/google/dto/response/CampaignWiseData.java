package com.caliper.campaign.google.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class CampaignWiseData {
	
	@JsonProperty("campaign_name")
	private String campaignName; //ad_campaign
	@JsonProperty("creation_date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date creationDate;
	@JsonProperty("cost")
	private double cost; //-- Clicks * CPC
	@JsonProperty("planned_cost")
	private BigDecimal plannedCost;
	@JsonProperty("delivered_impressions")
	private double deliveredImpressions; //ads_campaignbasicstats
	@JsonProperty("delivered_clicks")
	private double deliveredClicks; //ads_campaignbasicstats
	@JsonProperty("video_views")
	private double videoViews;
	@JsonProperty("conversions")
	private double conversions; //ads_campaignbasicstats
	@JsonProperty("ctr")
	private double ctr; //-- sum(clicks)/sum(impressions)
	@JsonProperty("vtr")
	private double vtr; //-- sum(clicks)/sum(impressions)
	@JsonProperty("cost_per_conversion")
	private double costPerConversion; //-- divide cost/conversion
	@JsonProperty("cost_per_mile")
	private double costPerMile; //-- divide cost/conversion
	@JsonProperty("cost_per_view")
	private double costPerView; //-- divide cost/conversion
	@JsonProperty("partner_name")
	private String partnerName; //ad_campaign

}
