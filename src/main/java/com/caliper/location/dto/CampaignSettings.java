package com.caliper.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CampaignSettings {

	private String clientBusinessName;
	private String industry;
	private String subIndustry;
	private Double radius;
	private String radiusUnit;
	private String clientEmail;
	private String clientCampaignPhoneNumber;
	private String callAdsPhoneNumber;
	private String landingPageUrl;
	private String youtubeUrl;
	private String platform;
}
