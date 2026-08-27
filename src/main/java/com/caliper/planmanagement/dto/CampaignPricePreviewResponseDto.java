package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignPricePreviewResponseDto {

    private Long campaignId;
    private String clientId;
    private CampaignPaymentBreakdownDto breakdown;
}
