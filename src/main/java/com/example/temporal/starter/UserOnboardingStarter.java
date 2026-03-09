package com.example.temporal.starter;

import com.example.temporal.model.UserRegistrationRequest;
import com.example.temporal.worker.UserOnboardingWorker;
import com.example.temporal.workflow.UserOnboardingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Starter: triggers a new User Onboarding workflow execution.
 *
 * Run with:  ./gradlew runStarter
 *
 * Prerequisites:
 *   1. temporal server start-dev  (separate terminal)
 *   2. ./gradlew runWorker        (separate terminal)
 */
public class UserOnboardingStarter {

    public static void main(String[] args) {
        String userId = "user-" + System.currentTimeMillis();

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(UserOnboardingWorker.TASK_QUEUE)
                .setWorkflowId("user-onboarding-" + userId)   // stable, deterministic ID
                .build();

        UserOnboardingWorkflow workflow = client.newWorkflowStub(
                UserOnboardingWorkflow.class, options);

        UserRegistrationRequest request = new UserRegistrationRequest(
                userId, "Alice Smith", "alice@example.com");

        // Start asynchronously — does not block waiting for workflow to complete
        WorkflowClient.start(workflow::startOnboarding, request);

        System.out.println("✓ Workflow started: user-onboarding-" + userId);
        System.out.println("  View in Temporal UI: http://localhost:8233");
        System.out.println("  Send email verification signal with:");
        System.out.println("    ./gradlew runSignal -PworkflowId=user-onboarding-" + userId);
    }
}
