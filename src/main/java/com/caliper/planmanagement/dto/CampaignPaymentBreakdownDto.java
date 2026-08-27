package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignPaymentBreakdownDto {

    private long campaignBudget;
    private long agencyCommission;
    private long taxableValue;
    private long sgst;
    private long cgst;
    private long grandTotal;
}
