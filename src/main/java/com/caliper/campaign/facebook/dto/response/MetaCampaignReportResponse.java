package com.caliper.campaign.facebook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaCampaignReportResponse {

    @JsonProperty("campaign_id")
    private Long campaignId;

    @JsonProperty("meta_campaign_id")
    private String metaCampaignId;

    @JsonProperty("campaign_name")
    private String campaignName;

    @JsonProperty("impressions")
    private String impressions;

    @JsonProperty("reach")
    private String reach;

    @JsonProperty("clicks")
    private String clicks;

    @JsonProperty("spend")
    private String spend;

    @JsonProperty("leads")
    private String leads;

    @JsonProperty("date_preset")
    private String datePreset;
}
