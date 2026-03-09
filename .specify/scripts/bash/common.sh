#!/usr/bin/env bash
# common.sh — shared utilities for temporal-user-app Spec-Kit scripts
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; BOLD='\033[1m'; RESET='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${RESET}  $*"; }
log_success() { echo -e "${GREEN}[OK]${RESET}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${RESET}  $*"; }
log_error()   { echo -e "${RED}[ERROR]${RESET} $*" >&2; }

get_repo_root() { git rev-parse --show-toplevel 2>/dev/null || { log_error "Not a git repo"; exit 1; }; }
get_current_branch() { git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown"; }

SPECIFY_DIR=".specify"
MEMORY_DIR="${SPECIFY_DIR}/memory"
SPECS_DIR="${SPECIFY_DIR}/specs"
TEMPLATES_DIR="${SPECIFY_DIR}/templates"

constitution_path() { echo "${MEMORY_DIR}/constitution.md"; }
feature_dir()       { echo "${SPECS_DIR}/${1:-$(get_current_branch)}"; }
spec_path()         { echo "$(feature_dir "$@")/spec.md"; }
plan_path()         { echo "$(feature_dir "$@")/plan.md"; }
tasks_path()        { echo "$(feature_dir "$@")/tasks.md"; }

require_file() {
    [[ -f "$1" ]] || { log_error "Required file not found: $1"; exit 1; }
}
require_constitution() { require_file "$(constitution_path)"; }
require_spec()         { require_file "$(spec_path)"; }
require_plan()         { require_file "$(plan_path)"; }

output_feature_context() {
    local branch; branch="$(get_current_branch)"
    local fdir; fdir="$(feature_dir "$branch")"
    cat <<EOF
{
  "BRANCH": "${branch}",
  "FEATURE_DIR": "${fdir}",
  "FEATURE_SPEC": "${fdir}/spec.md",
  "IMPL_PLAN":    "${fdir}/plan.md",
  "TASKS":        "${fdir}/tasks.md",
  "CONSTITUTION": "$(constitution_path)"
}
EOF
}
