package com.example.temporal.model;

/** Represents the current phase of a UserOnboarding workflow execution. Queryable via @QueryMethod. */
public enum OnboardingStatus {
    STARTED,
    AWAITING_VERIFICATION,
    VERIFIED,
    PROFILE_SETUP,
    COMPLETED
}
