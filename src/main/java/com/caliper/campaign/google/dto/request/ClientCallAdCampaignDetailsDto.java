package com.caliper.campaign.google.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientCallAdCampaignDetailsDto extends ClientCampaignDetailsDto{

	@JsonProperty("path_1")
	private String path1;
	
	@JsonProperty("path_2")
	private String path2;
}
