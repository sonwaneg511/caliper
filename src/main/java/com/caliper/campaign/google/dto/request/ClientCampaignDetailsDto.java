package com.caliper.campaign.google.dto.request;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientCampaignDetailsDto {
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("dealer_id")
	private String dealerId;
	
	@JsonProperty("campaign_name")
	private String campaignName;
	
	@JsonProperty("start_date")
	private Date startDate;
	
	@JsonProperty("end_date")
	private Date endDate;
	
	@JsonProperty("daily_budget")
	private BigDecimal dailyBudget;
	
	@JsonProperty("total_budget")
	private BigDecimal totalBudget;
	
	@JsonProperty("landing_page_url")
	private String landingPageUrl;
	
	@JsonProperty("youtube_url")
	private String youtubeUrl;
	
	@JsonProperty("headlines")
	private List<String> headlines;
	
	@JsonProperty("descriptions")
	private List<String> descriptions;
	
	@JsonProperty("platform")
	private String platform;
	
	@JsonProperty("client_comment")
	private String clientComment;
}
