package com.caliper.planmanagement.dto;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionDetailResponse {

    private String planName;
    private Date renewalDate;
    private double amount;
    private long locationCount;
    private List<String> purchasedModules;
    private Date expiresOn;
    private String status;
    private String durationType;
}
