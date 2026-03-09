#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"
cd "$(get_repo_root)"

BRANCH=$(get_current_branch)
cat > CLAUDE.md <<EOF
# CLAUDE.md — Agent Context
> Auto-generated $(date '+%Y-%m-%d'). Do not edit manually.

## Project
**Name**: temporal-user-app
**Branch**: \`${BRANCH}\`

## Constitution
Read first: \`.specify/memory/constitution.md\`

## Active Feature
- Spec:   \`.specify/specs/${BRANCH}/spec.md\`
- Plan:   \`.specify/specs/${BRANCH}/plan.md\`
- Tasks:  \`.specify/specs/${BRANCH}/tasks.md\`

## Build Commands
\`\`\`bash
./gradlew compileJava      # Compile
./gradlew test             # Run tests
./gradlew runWorker        # Start Temporal Worker (keep terminal open)
./gradlew runStarter       # Trigger workflow execution
./gradlew runSignal        # Send signal to running workflow
\`\`\`

## Temporal Local Server
\`\`\`bash
temporal server start-dev  # Start local Temporal (separate terminal)
# UI: http://localhost:8233
\`\`\`

## Package Layout
\`\`\`
com.example.temporal
├── workflow/   @WorkflowInterface + Impl (parent + child)
├── activity/   @ActivityInterface + Impl
├── worker/     Worker registration
├── starter/    main() entry points
└── model/      Plain Java data objects
\`\`\`

## Task Queue
All components share: \`"user-onboarding-queue"\`
EOF

log_success "Updated CLAUDE.md for branch: ${BRANCH}"
