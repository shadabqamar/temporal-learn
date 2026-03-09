#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

echo -e "\n${BOLD}╔══════════════════════════════════════╗"
echo -e "║  temporal-user-app Prerequisites     ║"
echo -e "╚══════════════════════════════════════╝${RESET}\n"

FAILED=0
check_tool() {
    if command -v "$2" &>/dev/null; then
        log_success "$1: $($2 ${3:---version} 2>&1 | head -1)"
    else
        log_error "$1: NOT FOUND ($2)"; FAILED=$((FAILED+1))
    fi
}

echo -e "${BOLD}── Tools ────────────────────────────────${RESET}"
check_tool "Git"          "git"      "--version"
check_tool "Java 17+"     "java"     "-version"
check_tool "Gradle"       "gradle"   "--version"
check_tool "Temporal CLI" "temporal" "--version"

echo -e "\n${BOLD}── Spec-Kit Files ───────────────────────${RESET}"
for f in \
    ".specify/memory/constitution.md" \
    ".specify/templates/spec-template.md" \
    ".specify/templates/plan-template.md" \
    ".specify/templates/tasks-template.md"; do
    [[ -f "$f" ]] && log_success "$f" || log_warn "MISSING: $f"
done

echo ""
[[ $FAILED -eq 0 ]] \
    && echo -e "${GREEN}${BOLD}✓ All prerequisites met.${RESET}" \
    || { echo -e "${RED}${BOLD}✗ ${FAILED} missing. Install before proceeding.${RESET}"; exit 1; }

echo -e "\n${BOLD}Tip:${RESET} Start a local Temporal server with:\n  temporal server start-dev\n"
