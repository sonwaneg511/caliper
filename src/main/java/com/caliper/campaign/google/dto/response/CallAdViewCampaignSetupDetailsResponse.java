package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallAdViewCampaignSetupDetailsResponse extends ViewCampaignSetupDetailsResponse{

	@JsonProperty("path_1")
	private String path1;
	
	@JsonProperty("path_2")
	private String path2;
}
