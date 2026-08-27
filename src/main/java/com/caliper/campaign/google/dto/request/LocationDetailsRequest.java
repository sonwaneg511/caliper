package com.caliper.campaign.google.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDetailsRequest {
	
	@JsonProperty("dealer_id")
	public String dealerId;
	
	@JsonProperty("fb_page_id")
	public String fbPageId;
	
	@JsonProperty("country_code")
	public String countryCode;

	@JsonProperty("call_ad_phone_number")
	public String callAdPhoneNumber;
	
	@JsonProperty("latitude")
	public String latitude;

	@JsonProperty("longitude")
	public String longitude;
	
	@JsonProperty("radius")
	public Double radius;
	
	@JsonProperty("landing_page_url")
	public String landingPageUrl;
	
	@JsonProperty("radius_unit")
	public String radiusUnit;
}
