package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignCreateOrderRequestDto {

    private String clientId;
    private String userId;
    private long campaignId;
}
