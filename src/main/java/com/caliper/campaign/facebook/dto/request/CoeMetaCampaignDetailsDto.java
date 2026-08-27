package com.caliper.campaign.facebook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoeMetaCampaignDetailsDto {

    @NotNull
    @JsonProperty("campaign_id")
    private Long campaignId;

    @JsonProperty("comment")
    private String comment;
}
