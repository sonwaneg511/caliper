package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDetailDto {

    @JsonProperty("objective")
    private String objective;

    @JsonProperty("advertising_phone_no")
    private String advertisingPhoneNo;

    @JsonProperty("latitude")
    private String latitude;

    @JsonProperty("longitude")
    private String longitude;

    @JsonProperty("campaign_coverage_radius")
    private String campaignCoverageRadius;

    @JsonProperty("landing_page_url")
    private String landingPageUrl;
}
