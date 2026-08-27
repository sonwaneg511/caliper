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
public class CampaignApiResponse {

    private boolean success;
    private String message;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("onboarding_step")
    private String onboardingStep;
}
