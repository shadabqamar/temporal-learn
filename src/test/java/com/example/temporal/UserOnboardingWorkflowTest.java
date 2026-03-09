package com.example.temporal;

import com.example.temporal.activity.ProfileActivitiesImpl;
import com.example.temporal.activity.UserActivitiesImpl;
import com.example.temporal.model.*;
import com.example.temporal.worker.UserOnboardingWorker;
import com.example.temporal.workflow.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for UserOnboardingWorkflow using TestWorkflowEnvironment.
 *
 * Demonstrates (per Article VIII):
 *  - Signal testing (emailVerified unblocks workflow)
 *  - Query testing (getStatus returns correct phase)
 *  - Child Workflow execution tested end-to-end
 *  - Activity execution verified implicitly by happy-path completion
 *
 * NO real Temporal server needed — TestWorkflowEnvironment is fully in-process.
 */
class UserOnboardingWorkflowTest {

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        client  = testEnv.getWorkflowClient();

        // Register both workflow impls and both activity impls on the test worker
        Worker worker = testEnv.newWorker(UserOnboardingWorker.TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                UserOnboardingWorkflowImpl.class,
                ProfileSetupChildWorkflowImpl.class
        );
        worker.registerActivitiesImplementations(
                new UserActivitiesImpl(),
                new ProfileActivitiesImpl()
        );

        testEnv.start();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private UserOnboardingWorkflow createWorkflowStub(String userId) {
        return client.newWorkflowStub(
                UserOnboardingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(UserOnboardingWorker.TASK_QUEUE)
                        .setWorkflowId("test-onboarding-" + userId)
                        .build()
        );
    }

    private UserRegistrationRequest sampleRequest(String userId) {
        return new UserRegistrationRequest(userId, "Alice Smith", "alice@example.com");
    }

    // ── Test 1: Happy path ────────────────────────────────────────────────────

    @Test
    @DisplayName("happy path: full onboarding completes after emailVerified signal")
    void fullOnboarding_completes_whenSignalReceived() {
        String userId = "user-001";
        UserOnboardingWorkflow workflow = createWorkflowStub(userId);

        // Start workflow asynchronously (it will block waiting for signal)
        WorkflowClient.start(workflow::startOnboarding, sampleRequest(userId));

        // Send emailVerified signal — unblocks the Workflow.await()
        workflow.emailVerified("token-abc-123");

        // Now block and get the result
        OnboardingResult result = workflow.startOnboarding(sampleRequest(userId));

        assertThat(result).isNotNull();
        assertThat(result.getFinalStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getMessage()).contains("Onboarding complete");
    }

    // ── Test 2: Query before signal ───────────────────────────────────────────

    @Test
    @DisplayName("getStatus() returns AWAITING_VERIFICATION while waiting for email signal")
    void getStatus_returnsAwaitingVerification_beforeSignal() {
        String userId = "user-002";
        UserOnboardingWorkflow workflow = createWorkflowStub(userId);

        // Start workflow (will block at AWAITING_VERIFICATION)
        WorkflowClient.start(workflow::startOnboarding, sampleRequest(userId));

        // Give the worker a moment to process the first two activities
        testEnv.sleep(java.time.Duration.ofSeconds(1));

        // Query while workflow is blocked — should be AWAITING_VERIFICATION
        OnboardingStatus status = workflow.getStatus();
        assertThat(status).isEqualTo(OnboardingStatus.AWAITING_VERIFICATION);

        // Clean up — send signal so the workflow can complete
        workflow.emailVerified("cleanup-token");
    }

    // ── Test 3: getUserId query ───────────────────────────────────────────────

    @Test
    @DisplayName("getUserId() query returns correct userId at any point")
    void getUserId_returnsCorrectUserId() {
        String userId = "user-003";
        UserOnboardingWorkflow workflow = createWorkflowStub(userId);

        WorkflowClient.start(workflow::startOnboarding, sampleRequest(userId));
        testEnv.sleep(java.time.Duration.ofMillis(500));

        assertThat(workflow.getUserId()).isEqualTo(userId);

        // Clean up
        workflow.emailVerified("cleanup-token");
    }

    // ── Test 4: Child workflow + signal end-to-end ────────────────────────────

    @Test
    @DisplayName("child workflow receives setDepartment signal and completes profile setup")
    void childWorkflow_completesProfileSetup_afterParentSignal() {
        String userId = "user-004";
        UserOnboardingWorkflow workflow = createWorkflowStub(userId);

        WorkflowClient.start(workflow::startOnboarding, sampleRequest(userId));
        workflow.emailVerified("token-xyz");

        OnboardingResult result = workflow.startOnboarding(sampleRequest(userId));

        // Child workflow ran — result message proves it
        assertThat(result.getFinalStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(result.getMessage()).contains("Profile setup complete");
        assertThat(result.getMessage()).contains("Engineering");
    }

    // ── Test 5: Timeout if signal never arrives ───────────────────────────────

    @Test
    @DisplayName("workflow returns AWAITING_VERIFICATION status when email verification times out")
    void workflow_timesOut_whenEmailVerificationNotReceived() {
        String userId = "user-005";
        UserOnboardingWorkflow workflow = createWorkflowStub(userId);

        WorkflowClient.start(workflow::startOnboarding, sampleRequest(userId));

        // Skip forward 25 hours in test time — triggers the 24h timeout
        testEnv.sleep(java.time.Duration.ofHours(25));

        OnboardingResult result = workflow.startOnboarding(sampleRequest(userId));

        assertThat(result.getFinalStatus()).isEqualTo(OnboardingStatus.AWAITING_VERIFICATION);
        assertThat(result.getMessage()).containsIgnoringCase("timed out");
    }
}
