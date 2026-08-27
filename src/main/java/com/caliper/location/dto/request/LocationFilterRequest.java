package com.caliper.location.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationFilterRequest {

	@JsonProperty("client_id")
	public String clientId;       //location level
	
	@JsonProperty("user_id")
	public String userId;
	
	public String state;         //location level
	
	public String country;         //location level
	
	public String city;          //location level
	
	@JsonProperty("dealer_id")
	public List<String> dealerId;        //location level

}
