package com.caliper.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkipConnectionRequest {

    @JsonProperty("client_id")
    private String clientId;

    private String platform;

    @JsonProperty("user_id")
    private String userId;
}
