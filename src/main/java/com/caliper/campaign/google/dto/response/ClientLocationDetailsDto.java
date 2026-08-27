package com.caliper.campaign.google.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientLocationDetailsDto {
	
	@JsonProperty("dealer_id")
	private String dealerId;
	
	@JsonProperty("state")
    private List<String> state;
	
	@JsonProperty("city")
    private List<String> city;
	
	@JsonProperty("pincode")
    private String pincode;
	
	@JsonProperty("address")
    private String address;
	
	private String latitude;
	
	private String longitude;
}
