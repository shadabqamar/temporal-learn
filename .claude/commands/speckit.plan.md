---
description: "Generate a technical Temporal implementation plan."
---
## /speckit.plan

1. Run `bash .specify/scripts/bash/update-agent-context.sh`
2. Read spec.md and constitution.md
3. Read `.specify/templates/plan-template.md`
4. **Constitution Check** — fill the ✅/⚠️/❌ table for Articles I–X
5. Write plan.md including:
   - ASCII architecture diagram showing Workflow → Activity / Child Workflow flow
   - Workflow Event Flow timeline (signals, timers, child workflows in order)
   - All `@WorkflowMethod`, `@SignalMethod`, `@QueryMethod` signatures
   - Activity method signatures with timeout + retry policy
   - Child Workflow interface with signals it accepts
   - Implementation phases + file inventory
6. Write `data-model.md` for all model POJOs
7. STOP — no tasks or code

**User Input**: `$ARGUMENTS`
