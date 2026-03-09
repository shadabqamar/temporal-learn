# Task Breakdown: [FEATURE_NAME]

**Feature ID**: [NNN-feature-name]
**Status**: Ready for Implementation
**Date**: [Date]

> Bottom-up order: models → activities → child workflow → parent workflow → worker → starters → tests
> `[P]` = safe for parallel execution

---

## Phase 1: Models

### Task [NNN]-T01: [Model class] `[P]`
**File**: `src/main/java/com/example/temporal/model/[Name].java`
**Done when**: Compiles, all fields present, serializable by Jackson
- [ ] Fields
- [ ] Constructor / getters / setters

---

## Phase 2: Activities

### Task [NNN]-T02: [ActivityInterface]
**File**: `...activity/[Name]Activities.java`
**Done when**: Interface compiles with @ActivityInterface, methods defined
- [ ] Interface with @ActivityInterface

### Task [NNN]-T03: [ActivityImpl] `[P]`
**File**: `...activity/[Name]ActivitiesImpl.java`
**Done when**: All methods implemented, no Workflow API calls
- [ ] Implementation

---

## Phase 3: Child Workflow

### Task [NNN]-T04: [ChildWorkflowInterface]
**File**: `...workflow/[Name]ChildWorkflow.java`
**Done when**: @WorkflowInterface, @WorkflowMethod, @SignalMethod defined
- [ ] Interface

### Task [NNN]-T05: [ChildWorkflowImpl]
**File**: `...workflow/[Name]ChildWorkflowImpl.java`
**Done when**: Signal handler updates state, Workflow.await used, deterministic
- [ ] Implementation

---

## Phase 4: Parent Workflow

### Task [NNN]-T06: [ParentWorkflowInterface]
**File**: `...workflow/[Name]Workflow.java`
**Done when**: @WorkflowInterface with @WorkflowMethod, @SignalMethod, @QueryMethod
- [ ] Interface

### Task [NNN]-T07: [ParentWorkflowImpl]
**File**: `...workflow/[Name]WorkflowImpl.java`
**Done when**: Orchestrates activities + child workflow, signals/queries work
- [ ] Implementation

---

## Phase 5: Worker

### Task [NNN]-T08: Worker
**File**: `...worker/[Name]Worker.java`
**Done when**: Registers all workflow + activity types, connects to Temporal, starts
- [ ] Register workflows and activities
- [ ] factory.start()

---

## Phase 6: Starters

### Task [NNN]-T09: WorkflowStarter `[P]`
**File**: `...starter/[Name]Starter.java`
**Done when**: Starts a workflow execution via WorkflowClient

### Task [NNN]-T10: SignalStarter `[P]`
**File**: `...starter/SendSignalStarter.java`
**Done when**: Sends a signal to a running workflow

---

## Phase 7: Tests

### Task [NNN]-T11: Workflow Tests
**File**: `src/test/java/com/example/temporal/[Name]WorkflowTest.java`
**Done when**: Happy path, signal handling, child workflow — all pass

---

## Checkpoints

- [ ] CP1: `./gradlew compileJava` — no errors
- [ ] CP2: `./gradlew runWorker` — Worker starts, connects to Temporal
- [ ] CP3: `./gradlew runStarter` — Workflow execution visible in Temporal UI
- [ ] CP4: `./gradlew runSignal` — Signal received, workflow advances
- [ ] CP5: `./gradlew test` — All tests GREEN
