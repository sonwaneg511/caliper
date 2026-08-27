package com.caliper.planmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyPaymentResponseDto {

    private boolean success;
    private Long planId;
    private String message;
    private String clientId;

    /**
     * The onboarding step the user should navigate to next.
     * Values: SOCIAL_ACCOUNT_SETUP | CAMPAIGN_SETUP | COMPLETED
     * Null for legacy clients who pre-date the onboarding feature.
     */
    @JsonProperty("onboarding_step")
    private String onboardingStep;

    /** Razorpay sub_XXX ID for the auto-enrolled Autopay subscription. Null if enrolment was skipped. */
    @JsonProperty("autopay_subscription_id")
    private String autopaySubscriptionId;

    /** Short URL the client must open to register their UPI/Card/NACH mandate. Null if enrolment was skipped. */
    @JsonProperty("autopay_auth_link")
    private String autopayAuthLink;
}
