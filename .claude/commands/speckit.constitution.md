---
description: "Define or update the Temporal project constitution."
---
## /speckit.constitution

1. Read `.specify/memory/constitution.md`
2. Read `.specify/templates/constitution-template.md`
3. Update constitution based on user input — ensure Temporal-specific Articles:
   - Article II: Temporal Architecture Rules (@WorkflowInterface, stubs, task queue)
   - Article III: Determinism (banned APIs in Workflow code)
   - Article IV: Signal Design (idempotency, Workflow.await)
   - Article V: Child Workflow Design (only when justified)
4. STOP — do not plan or code

**User Input**: `$ARGUMENTS`
