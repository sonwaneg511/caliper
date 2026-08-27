package com.caliper.campaign.google.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientSearchCampaignDetailsDto extends ClientCampaignDetailsDto{

	@JsonProperty("gemini_bool")
	private boolean geminiBool;
}
