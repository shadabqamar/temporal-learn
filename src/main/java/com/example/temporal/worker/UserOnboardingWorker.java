package com.example.temporal.worker;

import com.example.temporal.activity.ProfileActivitiesImpl;
import com.example.temporal.activity.UserActivitiesImpl;
import com.example.temporal.workflow.ProfileSetupChildWorkflowImpl;
import com.example.temporal.workflow.UserOnboardingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker entry point. Registers all Workflow and Activity types and starts polling.
 *
 * Run with:  ./gradlew runWorker
 * Keep this terminal open — the worker polls continuously.
 *
 * Prerequisite: temporal server start-dev  (in a separate terminal)
 */
public class UserOnboardingWorker {

    public static final String TASK_QUEUE = "user-onboarding-queue";

    private static final Logger log = LoggerFactory.getLogger(UserOnboardingWorker.class);

    public static void main(String[] args) {
        // Connect to the local Temporal development server
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // Create a worker bound to our task queue
        Worker worker = factory.newWorker(TASK_QUEUE);

        // ── Register Workflow implementations ─────────────────────────────
        worker.registerWorkflowImplementationTypes(
                UserOnboardingWorkflowImpl.class,
                ProfileSetupChildWorkflowImpl.class
        );

        // ── Register Activity implementations ─────────────────────────────
        worker.registerActivitiesImplementations(
                new UserActivitiesImpl(),
                new ProfileActivitiesImpl()
        );

        // Start polling for tasks
        factory.start();

        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  Worker started on queue: {}  ║", TASK_QUEUE);
        log.info("║  Temporal UI: http://localhost:8233              ║");
        log.info("╚══════════════════════════════════════════════════╝");
        log.info("Waiting for workflow tasks... (Ctrl+C to stop)");
    }
}
