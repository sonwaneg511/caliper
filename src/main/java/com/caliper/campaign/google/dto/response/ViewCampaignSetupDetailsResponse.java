package com.caliper.campaign.google.dto.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.caliper.campaign.google.entity.ClientDataSetupKeywords;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewCampaignSetupDetailsResponse {
	
	@JsonProperty("clientData")
	private ClientData clientDate;
	
	@JsonProperty("campaign_id")
	private Long campaignId;
	
	@JsonProperty("campaign_name")
	private String campaignName;
	
	@JsonProperty("client_comment")
	private String clientComment;
	
	@JsonProperty("star_date")
	private Date startDate;
	
	@JsonProperty("end_date")
	private Date endDate;
	
	private String platform;
	
	@JsonProperty("ad_phone_number")
	private String adPhoneNumber;
	
	@JsonProperty("match_type")
	private List<String> matchType;
	
	private List<String> network;
	
	@JsonProperty("bidding_strategy")
	private List<String> biddingStrategy;
	
	private String industry;
	
	@JsonProperty("sub_industry")
	private String subIndustry;
	
	@JsonProperty("client_location_setup")
	private ClientLocationSetup clientLocationSetup;
	
	@JsonProperty("final_url")
	private String finalUrl;
	
	@Column(name = "daily_budget")
	private BigDecimal dailyBudget;
	
	@Column(name = "total_budget")
	private BigDecimal totalBudget;
	
	@Column(name = "sub_industry_keywords")
	private List<ClientDataSetupKeywords> subIndustryKeywords;
	
	@Column(name = "url_keywords")
	private List<ClientDataSetupKeywords> urlKeywords;
	
	private List<String> headlines;
	
	private List<String> descriptions;

	public static final String MATCH_TYPE_EXACT = "Exact";
	public static final String MATCH_TYPE_PHRASE = "Phrase";
	public static final String MATCH_TYPE_BROAD = "Broad";
	
	public static final String MATCH_NETWORK_TARGET_GOOGLE_SEARCH = "Target Google Search";
	public static final String MATCH_NETWORK_TARGET_SEARCH_NETWORK = "Target Search Network";
	
	public static final String BIDDING_STRATERGY_MAXIMIZE_CLICKS = "Maximize Clicks";
	public static final String BIDDING_STRATERGY_MAXIMIZE_CONVERSIONS = "Maximize Conversions";
	
	
	
}
