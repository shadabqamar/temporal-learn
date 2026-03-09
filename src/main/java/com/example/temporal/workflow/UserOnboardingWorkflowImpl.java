package com.example.temporal.workflow;

import com.example.temporal.activity.UserActivities;
import com.example.temporal.model.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Parent Workflow implementation — orchestrates the full User Onboarding lifecycle.
 *
 * Temporal concepts demonstrated:
 *
 *  1. SIGNALS  — emailVerified() signal unblocks Workflow.await()
 *  2. QUERIES  — getStatus() and getUserId() expose live workflow state
 *  3. CHILD WORKFLOW — ProfileSetupChildWorkflow spawned after email verification
 *  4. PARENT → CHILD SIGNAL — parent sends setDepartment to child after child starts
 *  5. ASYNC CHILD — child started with Async.function(); parent waits for result
 *  6. DETERMINISM — no Thread.sleep, no UUID.randomUUID, no System.currentTimeMillis
 */
public class UserOnboardingWorkflowImpl implements UserOnboardingWorkflow {

    // ── Mutable workflow state (updated by signals and workflow logic) ────────
    private OnboardingStatus status = OnboardingStatus.STARTED;
    private String userId;
    private boolean emailVerificationReceived = false;
    private String verificationToken;

    // ── Activity stub (Article VI: explicit timeout + retry policy) ──────────
    private final UserActivities userActivities = Workflow.newActivityStub(
            UserActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    // ── Signal Handler ────────────────────────────────────────────────────────
    @Override
    public void emailVerified(String verificationToken) {
        Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                .info("Signal received: emailVerified for userId={}, token={}", userId, verificationToken);
        this.verificationToken = verificationToken;
        this.emailVerificationReceived = true;
    }

    // ── Query Handlers ────────────────────────────────────────────────────────
    @Override
    public OnboardingStatus getStatus() {
        return status;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    // ── Workflow Method ───────────────────────────────────────────────────────
    @Override
    public OnboardingResult startOnboarding(UserRegistrationRequest request) {
        this.userId = request.getUserId();

        Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                .info("Onboarding started for userId={}", userId);

        // ── Step 1: Validate user ─────────────────────────────────────────
        status = OnboardingStatus.STARTED;
        userActivities.validateUser(request);

        // ── Step 2: Send welcome email ────────────────────────────────────
        userActivities.sendWelcomeEmail(request.getEmail(), request.getName());

        // ── Step 3: Wait for email verification signal (max 24 hours) ────
        status = OnboardingStatus.AWAITING_VERIFICATION;
        Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                .info("Waiting for emailVerified signal (max 24h) for userId={}", userId);

        boolean verified = Workflow.await(
                Duration.ofHours(24),
                () -> emailVerificationReceived
        );

        if (!verified) {
            Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                    .warn("Email verification timed out for userId={}", userId);
            return new OnboardingResult(userId, OnboardingStatus.AWAITING_VERIFICATION,
                    "Onboarding timed out — email not verified within 24 hours");
        }

        status = OnboardingStatus.VERIFIED;
        Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                .info("Email verified for userId={}, token={}", userId, verificationToken);

        // ── Step 4: Spawn Child Workflow for profile setup ────────────────
        status = OnboardingStatus.PROFILE_SETUP;

        ProfileSetupChildWorkflow child = Workflow.newChildWorkflowStub(
                ProfileSetupChildWorkflow.class
        );

        ProfileSetupRequest childRequest = new ProfileSetupRequest(
                request.getUserId(),
                request.getName()
        );

        // Start child asynchronously (Article V: Async.function)
        Promise<String> childResult = Async.function(child::setupProfile, childRequest);

        // ── Step 5: Signal child after starting ───────────────────────────
        // In Temporal, signals are automatically buffered until child is ready
        // Proceed with signaling immediately after async start

        Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                .info("Child workflow started. Sending setDepartment signal.");

        // ── Step 6: Send signal to child workflow ─────────────────────────
        child.setDepartment("Engineering");

        // ── Step 7: Wait for child to complete ───────────────────────────
        String childOutput = childResult.get();
        Workflow.getLogger(UserOnboardingWorkflowImpl.class)
                .info("Child workflow completed: {}", childOutput);

        // ── Step 8: Return final result ───────────────────────────────────
        status = OnboardingStatus.COMPLETED;
        return new OnboardingResult(userId, OnboardingStatus.COMPLETED,
                "Onboarding complete. " + childOutput);
    }
}
