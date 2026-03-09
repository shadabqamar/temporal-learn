---
description: "Final acceptance gate for Temporal feature."
---
## /speckit.checklist

1. Read spec.md — every acceptance criterion
2. Read constitution.md — Articles II, III, IV, V, VIII
3. For each criterion: find code/test that satisfies it → ✅/⚠️/❌
4. Temporal-specific checks:
   - [ ] No banned APIs in Workflow code (Article III)
   - [ ] All signals tested (Article VIII)
   - [ ] Child Workflow justified (Article V)
   - [ ] Activity stubs have timeouts (Article VI)
   - [ ] Worker registers all types (Article II)
5. Run `./gradlew test` — report pass/fail count
6. Output: **READY TO MERGE** or **NEEDS WORK** with action items

**User Input**: `$ARGUMENTS`
