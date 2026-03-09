package com.example.temporal.workflow;

import com.example.temporal.model.OnboardingResult;
import com.example.temporal.model.OnboardingStatus;
import com.example.temporal.model.UserRegistrationRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Parent Workflow that orchestrates the full user onboarding lifecycle.
 *
 * Signals:
 *   emailVerified — sent by the user clicking a verification link
 *
 * Queries:
 *   getStatus  — returns current phase (safe, non-blocking)
 *   getUserId  — returns the userId being onboarded
 */
@WorkflowInterface
public interface UserOnboardingWorkflow {

    @WorkflowMethod
    OnboardingResult startOnboarding(UserRegistrationRequest request);

    /** External signal — sent when user clicks the email verification link. */
    @SignalMethod
    void emailVerified(String verificationToken);

    /** Safe, non-blocking read of current onboarding phase. */
    @QueryMethod
    OnboardingStatus getStatus();

    /** Safe, non-blocking read of the userId being onboarded. */
    @QueryMethod
    String getUserId();
}
