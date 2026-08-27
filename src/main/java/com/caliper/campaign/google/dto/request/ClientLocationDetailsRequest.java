package com.caliper.campaign.google.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientLocationDetailsRequest {
	
	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("industry")
	public String industry;
	
	@JsonProperty("sub_industry")
	public String subIndustry;
	
	@JsonProperty("location_details_request")
	public List<LocationDetailsRequest> locationDetailsRequest;
}
