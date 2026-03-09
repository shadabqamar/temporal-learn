#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

FEATURE_NAME="${1:-}"; [[ -z "$FEATURE_NAME" ]] && { log_error "Usage: $0 <feature-name>"; exit 1; }
SLUG=$(echo "$FEATURE_NAME" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd '[:alnum:]-')

cd "$(get_repo_root)"
mkdir -p "$SPECS_DIR"
LAST=$(ls -d "${SPECS_DIR}"/[0-9][0-9][0-9]-* 2>/dev/null | grep -oE '/[0-9]{3}-' | grep -oE '[0-9]{3}' | sort -n | tail -1 || echo "000")
NUM=$(printf "%03d" $((10#$LAST + 1)))
BRANCH="${NUM}-${SLUG}"
FDIR="${SPECS_DIR}/${BRANCH}"

echo -e "\n${BOLD}Creating feature: ${BRANCH}${RESET}"

git checkout -b "$BRANCH" 2>/dev/null || log_warn "Branch $BRANCH already exists"
mkdir -p "${FDIR}/contracts"

TMPL="${TEMPLATES_DIR}/spec-template.md"
TARGET="${FDIR}/spec.md"
[[ -f "$TMPL" ]] && cp "$TMPL" "$TARGET" || echo "# Spec: $FEATURE_NAME" > "$TARGET"
sed -i "s/\[FEATURE_NAME\]/${FEATURE_NAME}/g; s/\[NNN-feature-name\]/${BRANCH}/g; s/\[Date\]/$(date '+%B %Y')/g" "$TARGET" 2>/dev/null || true

log_success "Created branch: $BRANCH"
log_success "Spec dir: $FDIR"
echo ""
output_feature_context
