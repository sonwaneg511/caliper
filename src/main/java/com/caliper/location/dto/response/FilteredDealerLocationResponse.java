package com.caliper.location.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FilteredDealerLocationResponse {
	
	@JsonProperty(value = "country")
	public List<String> country;
	
	@JsonProperty(value = "state")
	public List<String> state;
	
	@JsonProperty(value = "city")
	public List<String> city;
	
	@JsonProperty("dealer_list")
	private Map<String, String> dealerList;

}
