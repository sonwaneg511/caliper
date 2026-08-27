package com.caliper.onboarding.entity;

public enum OnboardingStep {
    PLAN_PENDING,           // No active plan — user must select and purchase a plan
    SOCIAL_ACCOUNT_SETUP,   // POST or REVIEW purchased — needs GMB/Meta connection
    CAMPAIGN_SETUP,         // CAMPAIGN purchased — entered after social done/skipped
    COMPLETED               // All required steps done
}
