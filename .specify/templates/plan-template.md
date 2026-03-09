# Implementation Plan: [FEATURE_NAME]

**Feature ID**: [NNN-feature-name]
**Status**: Draft | Approved
**Date**: [Date]

---

## Constitution Check

| Article | Title | Status | Notes |
|---------|-------|--------|-------|
| I | Technology Stack | ✅ | |
| II | Temporal Architecture Rules | ✅ | |
| III | Determinism | ✅ | |
| IV | Signal Design | ✅ | |
| V | Child Workflow Design | ✅ | |
| VI | Activity Design | ✅ | |
| VII | Package Structure | ✅ | |
| VIII | Testing Standards | ✅ | |
| IX | Logging & Observability | ✅ | |
| X | Simplicity Gate | ✅ | |

---

## Architecture

```
[ASCII diagram of Workflow → Activity / Child Workflow flow]
```

## Workflow Event Flow

```
[Timeline of signals, activities, child workflows]
```

## Implementation Phases

### Phase 1: Models
- [ ] [Data objects]

### Phase 2: Activities
- [ ] [Activity interface + impl]

### Phase 3: Child Workflow
- [ ] [Child interface + impl]

### Phase 4: Parent Workflow
- [ ] [Parent interface + impl]

### Phase 5: Worker
- [ ] [Worker registration]

### Phase 6: Starters
- [ ] [WorkflowClient starters]

### Phase 7: Tests
- [ ] [TestWorkflowEnvironment tests]

## File Inventory

```
src/main/java/com/example/temporal/
├── model/
├── activity/
├── workflow/
├── worker/
└── starter/
```
