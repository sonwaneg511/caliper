package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDealerLocationDetails {

	@JsonProperty("dealer_id")
	private String dealerId;

	private String name;

	private String city;

	private String state;

	private String pincode;
}
