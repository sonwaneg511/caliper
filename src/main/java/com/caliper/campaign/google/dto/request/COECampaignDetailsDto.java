package com.caliper.campaign.google.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class COECampaignDetailsDto {

	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("campaign_id")
	private long campaignId;
	
	@JsonProperty("match_type")
	private String matchType;
	
	@JsonProperty("is_target_google_search")
	private boolean isTargetGoogleSearch;
	
	@JsonProperty("is_target_search_network")
	private boolean isTargetSearchNetwork;
	
	@JsonProperty("is_target_content_network")
	private boolean isTargetContentNetwork;
	
	@JsonProperty("is_target_partner_search_network")
	private boolean isTargetPartnerSearchNetwork;
	
	@JsonProperty("bidding_strategy")
	private String biddingStrategy;
	
	@JsonProperty("bidding_value")
	private String biddingValue;
	
	@JsonProperty("ad_name")
	private String adName;

	@JsonProperty("keywords")
	private List<String> keywords;

	@JsonProperty("headlines")
	private List<String> headlines;
	
	@JsonProperty("descriptions")
	private List<String> descriptions;
	
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String PINCODE = "pincode";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String RADIUS = "radius";
	public static final String RADIUS_UNIT = "radius_unit";
	public static final String COUNTRY = "country";
}
