package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignCreateOrderResponseDto {

    private String razorpayOrderId;
    private long amountPaise;
    private String currency;
    private String keyId;
    private String clientId;
    private Long campaignId;
    private CampaignPaymentBreakdownDto breakdown;
}
