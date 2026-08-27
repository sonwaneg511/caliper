package com.caliper.campaign.google.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientLocationSetupDto {

	@JsonProperty("dealer_id")
	public String dealerId;
	
	@JsonProperty("radius")
	public double radius;
	
	@JsonProperty("radius_unit")
	public String radiusUnit;
	
	@JsonProperty("call_ad_phone_number")
	public String callAdPhoneNumber;
	
	@JsonProperty("landing_page_url")
	public String landingPageUrl;
	
	@JsonProperty("latitude")
	public String latitude;
	
	@JsonProperty("longitude")
	public String longitude;
	
	@JsonProperty("client_campaign_phone_number")
	private String  clientCampaignPhoneNumber;
}
