# Feature Specification: User Onboarding

**Feature ID**: 001-user-onboarding
**Branch**: 001-user-onboarding
**Status**: Approved
**Date**: March 2026

---

## Feature Overview

When a new user registers, a durable multi-step onboarding workflow orchestrates:
validating the user's details, sending a welcome email, waiting for email verification
(via a **Signal**), and spawning a **Child Workflow** that handles profile setup.

### Objectives
- Durable, resumable onboarding — survives server restarts
- Email verification via external signal from the user clicking a link
- Profile setup isolated in a child workflow with its own retry policies
- Observable state via queries at any point

### Out of Scope
- Authentication / JWT
- Real email sending (mocked Activity)
- UI or REST API layer

---

## User Stories

### US-001-01: Start Onboarding
**As a** registration system,
**I want to** trigger a durable onboarding workflow for a new user,
**So that** the process survives crashes and runs to completion.

**Acceptance Criteria**:
- [x] Workflow starts with `UserRegistrationRequest` (userId, name, email)
- [x] Activities: validate user → send welcome email — executed in order
- [x] Workflow execution visible in Temporal UI with a stable workflowId

---

### US-001-02: Email Verification via Signal
**As a** user clicking a verification link,
**I want to** send an `emailVerified` signal to the running workflow,
**So that** onboarding unblocks and continues.

**Acceptance Criteria**:
- [x] Workflow blocks with `Workflow.await()` waiting for `emailVerified` signal
- [x] Signal carries a `verificationToken` string
- [x] After signal received, workflow records token and proceeds
- [x] Query `getStatus()` returns `"AWAITING_VERIFICATION"` before signal, `"VERIFIED"` after
- [x] If signal not received within 24h, workflow times out gracefully

---

### US-001-03: Profile Setup via Child Workflow
**As a** onboarding system,
**I want to** delegate profile setup to a child workflow,
**So that** it can run independently with its own event history and retry logic.

**Acceptance Criteria**:
- [x] Parent spawns `ProfileSetupChildWorkflow` after email is verified
- [x] Child workflow runs `createUserProfile` and `assignDefaultRole` activities
- [x] Parent sends `setDepartment` signal to child after child starts
- [x] Parent waits for child to complete before finishing
- [x] Child workflow visible as a separate execution in Temporal UI

---

### US-001-04: Query Workflow State
**As a** monitoring system,
**I want to** query the current status and user details of a running workflow,
**So that** I can display progress without interrupting execution.

**Acceptance Criteria**:
- [x] `getStatus()` returns current phase: `STARTED`, `AWAITING_VERIFICATION`, `VERIFIED`, `PROFILE_SETUP`, `COMPLETED`
- [x] `getUserId()` returns the userId at any point

---

## Temporal Concepts Used

| Concept | Purpose | Justification |
|---------|---------|---------------|
| Parent Workflow | Orchestrates the full onboarding lifecycle | Core durable process |
| Signal (`emailVerified`) | Receives external event from user clicking link | Only way to inject external input into a running workflow |
| Signal (`setDepartment`) | Sent from parent to child to configure department | Parent→child communication per Article IV |
| Query (`getStatus`) | Non-blocking state inspection | Safe read-only access to workflow state |
| Child Workflow (`ProfileSetupChildWorkflow`) | Handles profile creation with its own event history | Justified: could exceed event history limits; could run on separate workers in production |
| Activities | All I/O: validate, send email, create profile, assign role | All side effects must be in activities (Article III) |

---

## Review Checklist
- [x] All user stories have testable acceptance criteria
- [x] Child Workflow justified per Article V (own event history, separate worker potential)
- [x] All side effects in Activities (Article III compliant)
- [x] Signal design follows Article IV (Workflow.await used, state queryable)
- [x] No constitution violations
