package com.caliper.usermanagement.dto;

import java.util.Date;
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
public class ViewDealerDetailsResponse {

	private long id;
	
	@JsonProperty("dealer_id")
	private String dealerId;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("city")
	private String city;
	
	@JsonProperty("area")
	private String area;
	
	@JsonProperty("dealer_name")
	private String dealerName;
}
