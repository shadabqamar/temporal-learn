# Task Breakdown: User Onboarding

**Feature ID**: 001-user-onboarding
**Status**: Ready for Implementation

---

## Phase 1: Models

### Task 001-T01: UserRegistrationRequest `[P]`
**File**: `src/main/java/com/example/temporal/model/UserRegistrationRequest.java`
**Done when**: Compiles, 3 fields (userId, name, email), no-arg + all-arg constructors, getters/setters
- [ ] Fields: `String userId`, `String name`, `String email`
- [ ] No-arg constructor (required by Temporal serializer)
- [ ] All-arg constructor + getters/setters

### Task 001-T02: OnboardingStatus enum `[P]`
**File**: `src/main/java/com/example/temporal/model/OnboardingStatus.java`
**Done when**: 5 values: STARTED, AWAITING_VERIFICATION, VERIFIED, PROFILE_SETUP, COMPLETED
- [ ] `public enum OnboardingStatus { STARTED, AWAITING_VERIFICATION, VERIFIED, PROFILE_SETUP, COMPLETED }`

### Task 001-T03: OnboardingResult `[P]`
**File**: `src/main/java/com/example/temporal/model/OnboardingResult.java`
**Done when**: Compiles, fields: userId, finalStatus, message
- [ ] Fields + no-arg/all-arg constructors + getters/setters

### Task 001-T04: ProfileSetupRequest `[P]`
**File**: `src/main/java/com/example/temporal/model/ProfileSetupRequest.java`
**Done when**: Compiles, fields: userId, name, department; no-arg constructor present
- [ ] Fields + constructors + getters/setters

---

## Phase 2: Activities

### Task 001-T05: UserActivities interface
**File**: `src/main/java/com/example/temporal/activity/UserActivities.java`
**Done when**: `@ActivityInterface`, two methods defined
- [ ] `@ActivityInterface` annotation
- [ ] `void validateUser(UserRegistrationRequest request)`
- [ ] `void sendWelcomeEmail(String email, String name)`

### Task 001-T06: UserActivitiesImpl `[P]`
**File**: `src/main/java/com/example/temporal/activity/UserActivitiesImpl.java`
**Done when**: Both methods implemented with SLF4J logging; no Temporal/Workflow API calls
- [ ] `validateUser` — log + simulate validation (throw if email blank)
- [ ] `sendWelcomeEmail` — log "Sending welcome email to {email}"

### Task 001-T07: ProfileActivities interface `[P]`
**File**: `src/main/java/com/example/temporal/activity/ProfileActivities.java`
**Done when**: `@ActivityInterface`, two methods defined
- [ ] `void createUserProfile(String userId, String department)`
- [ ] `void assignDefaultRole(String userId)`

### Task 001-T08: ProfileActivitiesImpl `[P]`
**File**: `src/main/java/com/example/temporal/activity/ProfileActivitiesImpl.java`
**Done when**: Both methods implemented with SLF4J logging
- [ ] `createUserProfile` — log "Creating profile for {userId} in dept {department}"
- [ ] `assignDefaultRole` — log "Assigning default role to {userId}"

---

## Phase 3: Child Workflow

### Task 001-T09: ProfileSetupChildWorkflow interface
**File**: `src/main/java/com/example/temporal/workflow/ProfileSetupChildWorkflow.java`
**Done when**: `@WorkflowInterface`, one `@WorkflowMethod`, one `@SignalMethod`
- [ ] `@WorkflowInterface`
- [ ] `@WorkflowMethod String setupProfile(ProfileSetupRequest request)`
- [ ] `@SignalMethod void setDepartment(String department)`

### Task 001-T10: ProfileSetupChildWorkflowImpl
**File**: `src/main/java/com/example/temporal/workflow/ProfileSetupChildWorkflowImpl.java`
**Done when**: Signal sets department field; `Workflow.await` blocks until department non-null; activities called after
- [ ] `private String department = null` — mutable workflow state
- [ ] `setDepartment` signal handler sets `this.department`
- [ ] `setupProfile` calls `Workflow.await(() -> department != null)` then activities
- [ ] Activity stub created with `Workflow.newActivityStub(ProfileActivities.class, options)`
- [ ] Uses `Workflow.getLogger()` for logging
- [ ] Returns "Profile setup complete for {userId}"

---

## Phase 4: Parent Workflow

### Task 001-T11: UserOnboardingWorkflow interface
**File**: `src/main/java/com/example/temporal/workflow/UserOnboardingWorkflow.java`
**Done when**: All 4 annotations present, method signatures match plan.md
- [ ] `@WorkflowInterface`
- [ ] `@WorkflowMethod OnboardingResult startOnboarding(UserRegistrationRequest request)`
- [ ] `@SignalMethod void emailVerified(String verificationToken)`
- [ ] `@QueryMethod OnboardingStatus getStatus()`
- [ ] `@QueryMethod String getUserId()`

### Task 001-T12: UserOnboardingWorkflowImpl
**File**: `src/main/java/com/example/temporal/workflow/UserOnboardingWorkflowImpl.java`
**Done when**: Full orchestration: activities → await signal → child workflow → result; all checkpoints pass
- [ ] State fields: `OnboardingStatus status`, `String userId`, `boolean emailVerificationReceived`, `String verificationToken`
- [ ] `emailVerified` signal: sets `emailVerificationReceived = true`, stores token, updates status
- [ ] `getStatus()` and `getUserId()` query methods
- [ ] `startOnboarding`:
  - [ ] Set status = STARTED
  - [ ] Create UserActivities stub with `Workflow.newActivityStub()`
  - [ ] Call `validateUser(request)`
  - [ ] Call `sendWelcomeEmail(email, name)`
  - [ ] Set status = AWAITING_VERIFICATION
  - [ ] `Workflow.await(Duration.ofHours(24), () -> emailVerificationReceived)` — timeout after 24h
  - [ ] Set status = VERIFIED
  - [ ] Set status = PROFILE_SETUP
  - [ ] Create child stub: `Workflow.newChildWorkflowStub(ProfileSetupChildWorkflow.class)`
  - [ ] Start async: `Promise<String> childResult = Async.function(child::setupProfile, childRequest)`
  - [ ] Await child started: `child.getChildWorkflowExecution().get()`  ← CRITICAL — must do before signaling
  - [ ] Send signal to child: `child.setDepartment("Engineering")`
  - [ ] Wait for child: `childResult.get()`
  - [ ] Set status = COMPLETED
  - [ ] Return `new OnboardingResult(userId, COMPLETED, "Onboarding complete")`
- [ ] Use `Workflow.getLogger()` — NOT `LoggerFactory`
- [ ] NO `Thread.sleep`, `UUID.randomUUID`, `System.currentTimeMillis`

---

## Phase 5: Worker

### Task 001-T13: UserOnboardingWorker
**File**: `src/main/java/com/example/temporal/worker/UserOnboardingWorker.java`
**Done when**: Worker starts, registers all 4 types, connects to local Temporal
- [ ] `TASK_QUEUE = "user-onboarding-queue"` constant
- [ ] `WorkflowServiceStubs.newLocalServiceStubs()`
- [ ] `WorkflowClient.newInstance(service)`
- [ ] `WorkerFactory.newInstance(client)`
- [ ] `factory.newWorker(TASK_QUEUE)`
- [ ] Register: `UserOnboardingWorkflowImpl.class`, `ProfileSetupChildWorkflowImpl.class`
- [ ] Register activities: `new UserActivitiesImpl()`, `new ProfileActivitiesImpl()`
- [ ] `factory.start()` + log "Worker started on queue: user-onboarding-queue"

---

## Phase 6: Starters

### Task 001-T14: UserOnboardingStarter
**File**: `src/main/java/com/example/temporal/starter/UserOnboardingStarter.java`
**Done when**: Starts a workflow execution; prints workflowId; non-blocking
- [ ] Connect to local Temporal service
- [ ] Build `WorkflowOptions` with workflowId = `"user-onboarding-" + userId`
- [ ] Create typed stub: `client.newWorkflowStub(UserOnboardingWorkflow.class, options)`
- [ ] `WorkflowClient.start(workflow::startOnboarding, request)` — async, non-blocking
- [ ] Print "Workflow started: user-onboarding-{userId}"

### Task 001-T15: SendSignalStarter `[P]`
**File**: `src/main/java/com/example/temporal/starter/SendSignalStarter.java`
**Done when**: Sends emailVerified signal to a running workflow by workflowId
- [ ] Connect to Temporal service
- [ ] `client.newWorkflowStub(UserOnboardingWorkflow.class, workflowId)`
- [ ] Call `workflow.emailVerified("mock-token-12345")`
- [ ] Print "Signal sent to workflow: {workflowId}"

---

## Phase 7: Tests

### Task 001-T16: UserOnboardingWorkflowTest
**File**: `src/test/java/com/example/temporal/UserOnboardingWorkflowTest.java`
**Done when**: All 4 test methods pass with `./gradlew test`
- [ ] Use `TestWorkflowEnvironment.newInstance()` — NOT real server
- [ ] Register both workflow impls + both activity impls on test worker
- [ ] **Test 1** `@DisplayName("happy path: full onboarding completes")`:
  - Start workflow async via `WorkflowClient.start()`
  - Send `emailVerified` signal
  - Get result — assert `finalStatus == COMPLETED`
- [ ] **Test 2** `@DisplayName("getStatus returns AWAITING_VERIFICATION before signal")`:
  - Start workflow async
  - Sleep test env time slightly
  - Query `getStatus()` — assert `AWAITING_VERIFICATION`
- [ ] **Test 3** `@DisplayName("signal unblocks workflow and triggers child workflow")`:
  - Start workflow async
  - Send signal
  - Assert result not null, status COMPLETED
- [ ] **Test 4** `@DisplayName("child workflow receives setDepartment signal")`:
  - Run full flow
  - Verify `ProfileActivitiesImpl.createUserProfile` called (via spy or verify)
- [ ] `@AfterEach` closes `TestWorkflowEnvironment`

---

## Checkpoints

- [ ] **CP1**: `./gradlew compileJava` — GREEN, zero errors
- [ ] **CP2**: `./gradlew runWorker` — prints "Worker started on queue: user-onboarding-queue"
- [ ] **CP3**: `./gradlew runStarter` — workflow appears in Temporal UI at http://localhost:8233
- [ ] **CP4**: `./gradlew runSignal` — workflow advances past AWAITING_VERIFICATION in UI
- [ ] **CP5**: `./gradlew test` — 4 tests GREEN
- [ ] **CP6**: `./gradlew build` — full build GREEN
