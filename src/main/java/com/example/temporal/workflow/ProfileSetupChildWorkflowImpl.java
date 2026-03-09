package com.example.temporal.workflow;

import com.example.temporal.activity.ProfileActivities;
import com.example.temporal.model.ProfileSetupRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Child Workflow implementation for profile setup.
 *
 * Key Temporal patterns demonstrated:
 *  1. @SignalMethod updates mutable workflow state (department field)
 *  2. Workflow.await() blocks execution until the signal has been received
 *  3. Activity stub created via Workflow.newActivityStub() — never `new`
 *  4. Workflow.getLogger() used — NOT LoggerFactory (Article IX)
 */
public class ProfileSetupChildWorkflowImpl implements ProfileSetupChildWorkflow {

    // Mutable workflow state — updated by signal handler
    private String department = null;

    // Activity stub — created deterministically inside the workflow
    private final ProfileActivities profileActivities = Workflow.newActivityStub(
            ProfileActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(15))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(5)
                            .build())
                    .build()
    );

    // ── Signal Handler ──────────────────────────────────────────────────────
    @Override
    public void setDepartment(String department) {
        Workflow.getLogger(ProfileSetupChildWorkflowImpl.class)
                .info("Signal received: setDepartment={}", department);
        this.department = department;
    }

    // ── Workflow Method ─────────────────────────────────────────────────────
    @Override
    public String setupProfile(ProfileSetupRequest request) {
        Workflow.getLogger(ProfileSetupChildWorkflowImpl.class)
                .info("Child workflow started for userId={}", request.getUserId());

        // Block until the parent sends the setDepartment signal (Article IV)
        Workflow.await(() -> department != null);

        Workflow.getLogger(ProfileSetupChildWorkflowImpl.class)
                .info("Department received: {}. Proceeding with profile setup.", department);

        // Execute profile activities
        profileActivities.createUserProfile(request.getUserId(), department);
        profileActivities.assignDefaultRole(request.getUserId());

        String result = "Profile setup complete for userId=" + request.getUserId()
                + " in department=" + department;

        Workflow.getLogger(ProfileSetupChildWorkflowImpl.class).info(result);
        return result;
    }
}
