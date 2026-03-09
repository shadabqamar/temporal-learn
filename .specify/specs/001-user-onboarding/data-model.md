# Data Model: User Onboarding

**Feature ID**: 001-user-onboarding

---

## Model: UserRegistrationRequest

Input to the Parent Workflow `@WorkflowMethod`.

```java
public class UserRegistrationRequest {
    String userId;   // unique identifier
    String name;     // full name
    String email;    // email to verify
}
```

---

## Model: OnboardingStatus (enum)

```java
public enum OnboardingStatus {
    STARTED,
    AWAITING_VERIFICATION,
    VERIFIED,
    PROFILE_SETUP,
    COMPLETED
}
```

---

## Model: OnboardingResult

Returned by the parent `@WorkflowMethod`.

```java
public class OnboardingResult {
    String userId;
    OnboardingStatus finalStatus;
    String message;
}
```

---

## Model: ProfileSetupRequest

Input to `ProfileSetupChildWorkflow` `@WorkflowMethod`.

```java
public class ProfileSetupRequest {
    String userId;
    String name;
    String department;   // set later via @SignalMethod
}
```

---

## Signal Payloads

| Signal | Workflow | Payload |
|--------|----------|---------|
| `emailVerified` | Parent | `String verificationToken` |
| `setDepartment` | Child | `String department` |

---

## Query Return Types

| Query | Returns |
|-------|---------|
| `getStatus()` | `OnboardingStatus` |
| `getUserId()` | `String` |
