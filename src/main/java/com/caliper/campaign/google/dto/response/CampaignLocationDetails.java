package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignLocationDetails {

    @JsonProperty("dealer_id")
    private String dealerId;
    
    @JsonProperty("dealer_name")
    private String dealerName;

    @JsonProperty("landing_page_url")
    private String landingPageUrl;
    
    @JsonProperty("has_data")
    private boolean hasData;
}