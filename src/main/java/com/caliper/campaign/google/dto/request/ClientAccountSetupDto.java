package com.caliper.campaign.google.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClientAccountSetupDto {
	
	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("client_business_name")
	public String clientBusinessName;
	
	@JsonProperty("industry")
	public String industry;
	
	@JsonProperty("sub_industry")
	public String subIndustry;
	
	@JsonProperty("youtube_url")
	public String youtubeUrl;
	
	@JsonProperty("platform")
	public String platform;
	
	@JsonProperty("client_location_setup_list")
	public List<ClientLocationSetupDto> clientLocationSetupList;
	
	@JsonProperty("cpc_bid")
	public double cpcBid;
	
	@JsonProperty("keyword_source")
	public String keywordSource;

	@JsonProperty("monthly_budget")
	public String monthlyBudget;

	@JsonProperty("objective")
	public String objective;
}
