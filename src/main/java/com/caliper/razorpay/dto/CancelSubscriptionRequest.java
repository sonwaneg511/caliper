package com.caliper.razorpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CancelSubscriptionRequest {

    @JsonProperty("client_id")
    private String clientId;

    /** If true, cancels at end of current billing cycle; if false, cancels immediately */
    @JsonProperty("cancel_at_cycle_end")
    private boolean cancelAtCycleEnd;
}
