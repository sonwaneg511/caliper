package com.caliper.location.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GMBDealerList {
	
//	@JsonProperty("dealer_list")
//	private Map<String, String> dealerList;
	
	@JsonProperty("dealer_id")
	private String dealerId;
	
	@JsonProperty("dealer_name")
	private String dealerName;
	
	@JsonProperty("country")
	private String country;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("city")
	private String city;
}
