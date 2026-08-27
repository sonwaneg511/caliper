package com.caliper.razorpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("caliper_service_id")
    private Long caliperServiceId;

    /** monthly or yearly */
    @JsonProperty("billing_interval")
    private String billingInterval;

    /** Number of billing cycles; defaults to 12 for monthly, 3 for yearly */
    @JsonProperty("total_count")
    private Integer totalCount;
}
