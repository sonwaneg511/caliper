package com.caliper.usermanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ViewNewUserResponseDto {
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("roles")
	private List<String> roles;
	
	@JsonProperty("location_details")
	private List<LocationDetails> locationDetails;
}
