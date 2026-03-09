# temporal-user-app

A **Temporal + Java + Gradle** project demonstrating durable workflow orchestration,
built with **GitHub Spec-Kit** (Spec-Driven Development).

---

## What This Demonstrates

| Concept | Where |
|---------|-------|
| **Signals** | `emailVerified` signal unblocks `Workflow.await()` in the parent |
| **Child Workflows** | `ProfileSetupChildWorkflow` spawned by parent after verification |
| **Parent → Child Signal** | Parent sends `setDepartment` to child after child starts |
| **Queries** | `getStatus()` and `getUserId()` — non-blocking live state reads |
| **Determinism** | No `Thread.sleep`, `UUID.randomUUID`, `System.currentTimeMillis` |
| **TestWorkflowEnvironment** | Full test suite with no real Temporal server needed |

---

## Quick Start

### Prerequisites
```bash
# Install Temporal CLI
brew install temporal          # macOS
# or: https://docs.temporal.io/cli#installation

# Verify
temporal --version
java -version   # must be 17+
```

### 1. Start Temporal (Terminal 1)
```bash
temporal server start-dev
# UI: http://localhost:8233
```

### 2. Start Worker (Terminal 2)
```bash
./gradlew runWorker
# Prints: Worker started on queue: user-onboarding-queue
```

### 3. Trigger a Workflow (Terminal 3)
```bash
./gradlew runStarter
# Prints: ✓ Workflow started: user-onboarding-user-XXXXXXX
```

### 4. Send Email Verification Signal
```bash
WORKFLOW_ID=user-onboarding-user-XXXXXXX ./gradlew runSignal
# Prints: ✓ Signal 'emailVerified' sent
```

### 5. Watch it complete in the UI
Open: **http://localhost:8233** — you'll see both the parent and child workflow executions.

---

## Run Tests (no Temporal server needed)
```bash
./gradlew test
# 5 tests — happy path, queries, child workflow, timeout
```

---

## Workflow Architecture

```
UserOnboardingWorkflow (Parent)
  │
  ├── Activity: validateUser
  ├── Activity: sendWelcomeEmail
  ├── Workflow.await(emailVerified Signal) ◄── external signal
  │
  └── ProfileSetupChildWorkflow (Child)
        │
        ├── Workflow.await(setDepartment Signal) ◄── from parent
        ├── Activity: createUserProfile
        └── Activity: assignDefaultRole
```

---

## Spec-Kit SDD Workflow

```
/speckit.constitution  → .specify/memory/constitution.md     (done ✅)
/speckit.specify       → .specify/specs/001-user-onboarding/spec.md  (done ✅)
/speckit.plan          → plan.md + data-model.md              (done ✅)
/speckit.tasks         → tasks.md (T01–T16 + checkpoints)    (done ✅)
/speckit.implement     → all Java source files                (done ✅)
/speckit.checklist     → final acceptance gate
```

---

## Project Structure

```
temporal-user-app/
├── CLAUDE.md                                ← Agent context
├── .specify/
│   ├── memory/constitution.md               ← 10 Articles (supreme law)
│   ├── templates/                           ← spec/plan/tasks templates
│   ├── scripts/bash/                        ← create-new-feature, update-context, etc.
│   └── specs/001-user-onboarding/
│       ├── spec.md                          ← What to build
│       ├── plan.md                          ← How + architecture diagram
│       ├── tasks.md                         ← T01–T16 + checkpoints
│       └── data-model.md                   ← POJOs + signal payloads
├── .claude/commands/                        ← /speckit.* slash commands
├── build.gradle
└── src/
    ├── main/java/com/example/temporal/
    │   ├── workflow/   (parent + child)
    │   ├── activity/   (user + profile)
    │   ├── worker/
    │   ├── starter/
    │   └── model/
    └── test/java/com/example/temporal/
        └── UserOnboardingWorkflowTest.java  (5 tests)
```
