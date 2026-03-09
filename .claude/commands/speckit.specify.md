---
description: "Create a Temporal feature specification."
---
## /speckit.specify

1. Run `bash .specify/scripts/bash/create-new-feature.sh "<feature-name>"`
2. Read `.specify/memory/constitution.md`
3. Read `.specify/templates/spec-template.md`
4. Write spec.md — include a **Temporal Concepts Used** table:
   - Which Workflows, Activities, Signals, Child Workflows, Queries are needed
   - Justify each Child Workflow against Article V (NOT for code organisation)
5. Mark ambiguous areas `[NEEDS CLARIFICATION]`
6. STOP — no planning or coding

**Rules**:
- Describe WHAT and WHY — never HOW
- Signal flows must be described in user story terms, not Java terms
- Every acceptance criterion must be independently testable

**User Input**: `$ARGUMENTS`
