---
description: "Execute tasks to implement the Temporal feature."
---
## /speckit.implement

**Prerequisites** (verify all exist):
- `.specify/memory/constitution.md` ✅
- `.specify/specs/NNN/spec.md` ✅
- `.specify/specs/NNN/plan.md` ✅
- `.specify/specs/NNN/tasks.md` ✅

**Instructions**:
1. Read tasks.md — find first unchecked task
2. Re-read constitution.md Articles II, III, IV, V before every Workflow/Activity file
3. Implement task → check it off `[x]` in tasks.md
4. At every Checkpoint: verify the stated condition before continuing
5. After all tasks: run `./gradlew build` then `./gradlew test`
6. Report completion summary

**Temporal-specific rules**:
- NEVER use `new` to create workflow/activity stubs — always use `Workflow.newChildWorkflowStub()` / `Workflow.newActivityStub()`
- NEVER call `Thread.sleep()` — use `Workflow.sleep()`
- NEVER use `UUID.randomUUID()` in Workflow — use `Workflow.randomUUID()`
- ALWAYS await child workflow start before sending signals to it
- ALWAYS use `Workflow.getLogger()` inside workflow code

**User Input**: `$ARGUMENTS`
