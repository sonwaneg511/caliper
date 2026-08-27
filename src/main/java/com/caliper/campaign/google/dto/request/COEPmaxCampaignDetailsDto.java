package com.caliper.campaign.google.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class COEPmaxCampaignDetailsDto extends COECampaignDetailsDto{
	
	@JsonProperty("long_headlines")
	private List<String> longHeadlines;

}
