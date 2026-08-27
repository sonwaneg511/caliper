package com.caliper.planmanagement.dto;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionHistoryResponse {

    private Long planId;
    private String planName;
    private Date billingDate;
    private double amount;
    private String status;
}
