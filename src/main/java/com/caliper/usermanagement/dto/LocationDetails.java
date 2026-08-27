package com.caliper.usermanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LocationDetails {
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("dealer_name")
	private String dealerName;
	
	@JsonProperty("city")
	private String city;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("area")
	private String area;
	
	@JsonProperty("dealer_id")
	private String dealerId;
}
