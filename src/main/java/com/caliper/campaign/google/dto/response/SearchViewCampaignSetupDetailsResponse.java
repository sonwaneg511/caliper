package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchViewCampaignSetupDetailsResponse extends ViewCampaignSetupDetailsResponse{
	
	@JsonProperty("ad_name")
	private String adName;

}
