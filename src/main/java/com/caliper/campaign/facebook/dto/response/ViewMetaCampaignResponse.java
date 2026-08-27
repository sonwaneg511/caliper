package com.caliper.campaign.facebook.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewMetaCampaignResponse {

    @JsonProperty("campaign_id")
    private Long campaignId;

    @JsonProperty("campaign_name")
    private String campaignName;

    @JsonProperty("objective")
    private String objective;

    @JsonProperty("status")
    private String status;

    @JsonProperty("daily_budget")
    private BigDecimal dailyBudget;

    @JsonProperty("total_budget")
    private BigDecimal totalBudget;

    @JsonProperty("start_time")
    private Date startTime;

    @JsonProperty("stop_time")
    private Date stopTime;

    @JsonProperty("dealer_id")
    private String dealerId;

    @JsonProperty("meta_campaign_id")
    private String metaCampaignId;

    @JsonProperty("coe_comment")
    private String coeComment;

    @JsonProperty("error_comment")
    private String errorComment;

    @JsonProperty("client_comment")
    private String clientComment;

    @JsonProperty("created_by")
    private String createdBy;
}
