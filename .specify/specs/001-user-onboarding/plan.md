# Implementation Plan: User Onboarding

**Feature ID**: 001-user-onboarding
**Status**: Approved
**Date**: March 2026

---

## Constitution Check

| Article | Title | Status | Notes |
|---------|-------|--------|-------|
| I | Technology Stack | ✅ | Java 17, Temporal 1.33.0, Gradle 9.4 |
| II | Temporal Architecture Rules | ✅ | @WorkflowInterface, stubs, shared TASK_QUEUE |
| III | Determinism | ✅ | All I/O in Activities; Workflow.await, Workflow.sleep |
| IV | Signal Design | ✅ | emailVerified uses Workflow.await; state queryable |
| V | Child Workflow Design | ✅ | Justified: own event history + potential separate worker |
| VI | Activity Design | ✅ | Each activity does one thing; StartToCloseTimeout set |
| VII | Package Structure | ✅ | model/activity/workflow/worker/starter |
| VIII | Testing Standards | ✅ | TestWorkflowEnvironment, signal tests, child workflow tests |
| IX | Logging & Observability | ✅ | Workflow.getLogger() in workflows; SLF4J in activities |
| X | Simplicity Gate | ✅ | 1 Worker, no DI framework, plain main() starters |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                   UserOnboardingWorkflow                      │
│  (Parent Workflow — task queue: user-onboarding-queue)       │
│                                                              │
│  1. validateUser(request)  ──────────────► UserActivities   │
│  2. sendWelcomeEmail(email) ─────────────► UserActivities   │
│  3. Workflow.await(emailVerified signal)                     │
│       ▲  Signal: emailVerified(token)                        │
│       │  (from external system / SendSignalStarter)         │
│  4. Workflow.newChildWorkflowStub(ProfileSetupChildWorkflow) │
│  5. Async.function(child::setupProfile, request)            │
│  6. await child start → send setDepartment signal to child  │
│  7. child.getChildWorkflowExecution().get() [started]       │
│  8. child.setDepartment("Engineering")                       │
│  9. greeting.get()  [child completes]                        │
│  10. return OnboardingResult(COMPLETED)                      │
└──────────────────────────────────────────────────────────────┘
         │ spawns
         ▼
┌──────────────────────────────────────────────────────────────┐
│                 ProfileSetupChildWorkflow                     │
│  (Child Workflow — same task queue)                          │
│                                                              │
│  1. Workflow.await(department != null)  ◄── Signal:         │
│                                             setDepartment   │
│  2. createUserProfile(userId, dept)  ────► ProfileActivities│
│  3. assignDefaultRole(userId)  ──────────► ProfileActivities│
│  4. return "Profile setup complete"                          │
└──────────────────────────────────────────────────────────────┘
```

---

## Workflow Event Timeline

```
t=0  WorkflowStarted
t=1  ActivityScheduled: validateUser
t=2  ActivityCompleted: validateUser
t=3  ActivityScheduled: sendWelcomeEmail
t=4  ActivityCompleted: sendWelcomeEmail
t=5  WorkflowExecutionSignaled: emailVerified  ← (external, any time)
t=6  ChildWorkflowExecutionInitiated: ProfileSetupChildWorkflow
t=7  ChildWorkflowExecutionStarted
t=8  WorkflowExecutionSignaled: setDepartment  → forwarded to child
t=9  ActivityScheduled: createUserProfile      (inside child)
t=10 ActivityCompleted: createUserProfile
t=11 ActivityScheduled: assignDefaultRole
t=12 ActivityCompleted: assignDefaultRole
t=13 ChildWorkflowExecutionCompleted
t=14 WorkflowExecutionCompleted
```

---

## Interfaces & Method Signatures

### UserOnboardingWorkflow (Parent)
```java
@WorkflowInterface
interface UserOnboardingWorkflow {
    @WorkflowMethod
    OnboardingResult startOnboarding(UserRegistrationRequest request);

    @SignalMethod
    void emailVerified(String verificationToken);

    @QueryMethod
    OnboardingStatus getStatus();

    @QueryMethod
    String getUserId();
}
```

### ProfileSetupChildWorkflow (Child)
```java
@WorkflowInterface
interface ProfileSetupChildWorkflow {
    @WorkflowMethod
    String setupProfile(ProfileSetupRequest request);

    @SignalMethod
    void setDepartment(String department);
}
```

### UserActivities
```java
@ActivityInterface
interface UserActivities {
    void validateUser(UserRegistrationRequest request);
    void sendWelcomeEmail(String email, String name);
}
```

### ProfileActivities
```java
@ActivityInterface
interface ProfileActivities {
    void createUserProfile(String userId, String department);
    void assignDefaultRole(String userId);
}
```

---

## Activity Options

```java
// UserActivities stub
ActivityOptions userOptions = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(10))
    .setRetryOptions(RetryOptions.newBuilder()
        .setMaximumAttempts(3)
        .build())
    .build();

// ProfileActivities stub
ActivityOptions profileOptions = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(15))
    .setRetryOptions(RetryOptions.newBuilder()
        .setMaximumAttempts(5)
        .build())
    .build();
```

---

## Implementation Phases

### Phase 1: Models
- [ ] `UserRegistrationRequest.java`
- [ ] `OnboardingStatus.java` (enum)
- [ ] `OnboardingResult.java`
- [ ] `ProfileSetupRequest.java`

### Phase 2: Activity Interfaces + Implementations
- [ ] `UserActivities.java` + `UserActivitiesImpl.java`
- [ ] `ProfileActivities.java` + `ProfileActivitiesImpl.java`

### Phase 3: Child Workflow
- [ ] `ProfileSetupChildWorkflow.java` (interface)
- [ ] `ProfileSetupChildWorkflowImpl.java`

### Phase 4: Parent Workflow
- [ ] `UserOnboardingWorkflow.java` (interface)
- [ ] `UserOnboardingWorkflowImpl.java`

### Phase 5: Worker
- [ ] `UserOnboardingWorker.java` — registers all 4 types, starts factory

### Phase 6: Starters
- [ ] `UserOnboardingStarter.java` — starts a workflow execution
- [ ] `SendSignalStarter.java` — sends emailVerified signal

### Phase 7: Tests
- [ ] `UserOnboardingWorkflowTest.java`

---

## File Inventory

```
src/main/java/com/example/temporal/
├── model/
│   ├── UserRegistrationRequest.java
│   ├── OnboardingStatus.java
│   ├── OnboardingResult.java
│   └── ProfileSetupRequest.java
├── activity/
│   ├── UserActivities.java
│   ├── UserActivitiesImpl.java
│   ├── ProfileActivities.java
│   └── ProfileActivitiesImpl.java
├── workflow/
│   ├── UserOnboardingWorkflow.java
│   ├── UserOnboardingWorkflowImpl.java
│   ├── ProfileSetupChildWorkflow.java
│   └── ProfileSetupChildWorkflowImpl.java
├── worker/
│   └── UserOnboardingWorker.java
└── starter/
    ├── UserOnboardingStarter.java
    └── SendSignalStarter.java

src/test/java/com/example/temporal/
└── UserOnboardingWorkflowTest.java
```
