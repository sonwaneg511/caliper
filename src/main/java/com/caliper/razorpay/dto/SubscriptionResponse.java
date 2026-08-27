package com.caliper.razorpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class SubscriptionResponse {

    private int status;
    private String message;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("razorpay_subscription_id")
    private String razorpaySubscriptionId;

    @JsonProperty("auth_link")
    private String authLink;

    @JsonProperty("subscription_status")
    private String subscriptionStatus;

    @JsonProperty("billing_interval")
    private String billingInterval;

    // GST breakdown fields
    @JsonProperty("base_amount_rupees")
    private Double baseAmountRupees;

    @JsonProperty("cgst_amount_rupees")
    private Double cgstAmountRupees;

    @JsonProperty("sgst_amount_rupees")
    private Double sgstAmountRupees;

    @JsonProperty("total_amount_rupees")
    private Double totalAmountRupees;

    @JsonProperty("cgst_rate")
    private BigDecimal cgstRate;

    @JsonProperty("sgst_rate")
    private BigDecimal sgstRate;

    @JsonProperty("paid_count")
    private Integer paidCount;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("next_charge_at")
    private Date nextChargeAt;

    private Date timestamp;
}
