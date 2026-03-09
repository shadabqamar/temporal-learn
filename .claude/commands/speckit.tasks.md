---
description: "Break Temporal plan into ordered tasks."
---
## /speckit.tasks

1. Read plan.md and data-model.md
2. Read `.specify/templates/tasks-template.md`
3. Write tasks.md — STRICT bottom-up order:
   - Models (POJOs) → Activities (interface then impl) → Child Workflow (interface then impl) → Parent Workflow → Worker → Starters → Tests
4. Each task has: file path, done-when, subtask checklist
5. Mark `[P]` on tasks safe to do in parallel
6. Include checkpoints: compileJava → runWorker → runStarter → runSignal → test
7. STOP — no implementation

**User Input**: `$ARGUMENTS`
