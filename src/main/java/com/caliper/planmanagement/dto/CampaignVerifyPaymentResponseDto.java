package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignVerifyPaymentResponseDto {

    private boolean success;
    private Long campaignId;
    private String message;
    private String clientId;
    private String campaignStatus;
}
