package com.caliper.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViewAllLocations {

	@JsonProperty("dealer_id")
	private String dealerId;
	
	@JsonProperty("dealer_name")
	private String dealerName;
	
	private String area;
	
	private String city;
	
	private String state;
	
}
