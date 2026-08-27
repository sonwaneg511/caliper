package com.caliper.campaign.google.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopulateClientOnboardingDetailsDto {

	@JsonProperty("industry_vs_subindustry")
	Map<String,List<String>> industryVSSubIndustryMap;
	
	@JsonProperty("dealer_ids")
	List<String>dealerIds;
	
	//Map<String,String> locations;
	List<CampaignDealerLocationDetails>locations;
	
	@JsonProperty("radius_metric")
	List<String>radiusMetric;
	
	//List<String>platform;
	
	@JsonProperty("country_code")
	String countryCode;
	
	@JsonProperty("phone_number")
	String phoneNumber;
	
	@JsonProperty("client_name")
	String clientName;
}
