package com.caliper.onboarding.entity;

public enum ConnectionStatus {
    PENDING,    // Not yet acted upon
    CONNECTED,  // OAuth completed successfully
    FAILED,     // OAuth callback failed — user must retry or skip
    SKIPPED     // User explicitly skipped this platform for now
}
