package com.example.temporal.starter;

import com.example.temporal.workflow.UserOnboardingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Starter: sends the 'emailVerified' signal to a running onboarding workflow.
 *
 * Run with:  ./gradlew runSignal
 *
 * To target a specific workflow, set the WORKFLOW_ID env variable:
 *   WORKFLOW_ID=user-onboarding-user-1234 ./gradlew runSignal
 *
 * This simulates the user clicking their email verification link.
 */
public class SendSignalStarter {

    public static void main(String[] args) {
        // Determine workflowId — from env var or fallback to listing
        String workflowId = System.getenv("WORKFLOW_ID");
        if (workflowId == null || workflowId.isBlank()) {
            // Fallback: use first arg if provided
            workflowId = args.length > 0 ? args[0] : null;
        }
        if (workflowId == null || workflowId.isBlank()) {
            System.err.println("ERROR: Set WORKFLOW_ID env variable to the target workflow ID.");
            System.err.println("  e.g. WORKFLOW_ID=user-onboarding-user-123 ./gradlew runSignal");
            System.err.println("  Or check http://localhost:8233 for the workflow ID.");
            System.exit(1);
        }

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // Get a stub for the already-running workflow by its ID
        UserOnboardingWorkflow workflow = client.newWorkflowStub(
                UserOnboardingWorkflow.class, workflowId);

        String token = "verify-token-" + System.currentTimeMillis();

        // Send the signal — workflow unblocks from Workflow.await()
        workflow.emailVerified(token);

        System.out.println("✓ Signal 'emailVerified' sent to workflow: " + workflowId);
        System.out.println("  Token: " + token);
        System.out.println("  Watch the workflow advance in: http://localhost:8233");
    }
}
