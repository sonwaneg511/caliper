package com.caliper.campaign.facebook.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MetaCampaignFilterRequest {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("dealer_id")
    private List<String> dealerId;

    @JsonProperty("page_no")
    private int pageNo;

    private String search;
}
