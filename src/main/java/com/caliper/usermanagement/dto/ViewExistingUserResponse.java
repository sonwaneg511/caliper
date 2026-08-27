package com.caliper.usermanagement.dto;

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
public class ViewExistingUserResponse {

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("dealer_ids")
	List<String> dealerIds;

	@JsonProperty("roles")
	String roles;
	
	@JsonProperty("view_dealer_details_response_list")
	List<ViewDealerDetailsResponse> viewDealerDetailsResponseList;
	
	@JsonProperty("modules")
	List<String> modules;


}
