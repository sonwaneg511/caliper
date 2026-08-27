package com.caliper.campaign.google.dto.request;

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
public class ClientPmaxCampaignDetailsDto extends ClientCampaignDetailsDto{

	@JsonProperty("long_headlines")
	private List<String> longHeadlines;
	
	@JsonProperty("marketing_images")
	private List<String> marketingImages;
	
	@JsonProperty("square_marketing_images")
	private List<String> squareMarketingImages;
	
	@JsonProperty("portrait_marketing_images")
	private List<String> portraitMarketingImages;
	
	@JsonProperty("logo")
	private List<String> logo;
	
	@JsonProperty("landscape_logo")
	private List<String> landscapeLogo;
	
	@JsonProperty("business_name")
	private String businessName;

}
