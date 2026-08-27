package com.caliper.onboarding.service;

import org.springframework.stereotype.Component;

import com.caliper.onboarding.entity.OnboardingStep;

@Component
public class OnboardingStepResolver {

    public OnboardingStep resolveStep(boolean requiresSocial, boolean requiresCampaign) {
        if (requiresSocial)   return OnboardingStep.SOCIAL_ACCOUNT_SETUP;
        if (requiresCampaign) return OnboardingStep.CAMPAIGN_SETUP;
        return OnboardingStep.COMPLETED;
    }
}
