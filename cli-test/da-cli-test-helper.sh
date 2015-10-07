#!/bin/bash

# ==============================================================================
# Helper methods
# ==============================================================================

# ------------------------------------------------------------------------------
# Usage: add_list_admin [white|black] G:A:V
# ------------------------------------------------------------------------------
add_list_admin() {
    local list_type="$1"
    local gav="$2"
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh add  "${list_type}" "${gav}")"
}

# ------------------------------------------------------------------------------
# Usage: check_whitelist_admin [white|black] G:A:V
# ------------------------------------------------------------------------------
check_list_admin() {
    local list_type="$1"
    local gav="$2"
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh check "${list_type}" "${gav}")"
}

# ------------------------------------------------------------------------------
# Usage: check_list [white|black] G:A:V
# ------------------------------------------------------------------------------
check_list() {
    local list_type="$1"
    local gav="$2"
    echo "$(${SCRIPT_DIR}/da-cli.sh check "${list_type}" "${gav}")"
}

# ------------------------------------------------------------------------------
# Usage: list_admin [white|black]
# ------------------------------------------------------------------------------
list_admin() {
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh list "$1")"
}
# ------------------------------------------------------------------------------
# Usage: list [white|black]
# ------------------------------------------------------------------------------
list() {
    echo "$(${SCRIPT_DIR}/da-cli.sh list "$1")"
}

# ------------------------------------------------------------------------------
# Usage: delete_list [white|black] G:A:V
# ------------------------------------------------------------------------------
delete_list_admin() {
    local list_type="$1"
    local gav="$2"
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh delete "${list_type}" "${gav}")"
}

# ------------------------------------------------------------------------------
# Usage: lookup_admin G:A:V
#
# Find corresponding redhat version
# ------------------------------------------------------------------------------
lookup_admin() {
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh lookup "$1")"
}

# ------------------------------------------------------------------------------
# Usage: lookup G:A:V
#
# Find corresponding redhat version
# ------------------------------------------------------------------------------
lookup() {
    echo "$(${SCRIPT_DIR}/da-cli.sh lookup "$1")"
}

# ------------------------------------------------------------------------------
# Usage: report_admin G:A:V [--raw|--json]
#
# Generate dependency report for G:A:V
# ------------------------------------------------------------------------------
report_admin() {
    local gav="$1"
    local report_type="$2"
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh report "$2" "$1")"
}

# ------------------------------------------------------------------------------
# Usage: report G:A:V [--raw|--json]
#
# Generate dependency report for G:A:V
# ------------------------------------------------------------------------------
report() {
    local gav="$1"
    local report_type="$2"
    echo "$(${SCRIPT_DIR}/da-cli.sh report "$2" "$1")"
}

# ------------------------------------------------------------------------------
# Usage: pom_bw_admin PATH [--transitive]
#
# Get all dependencies from pom in PATH and print the Black/white list status
# ------------------------------------------------------------------------------
pom_bw_admin() {
    local path="$1"
    local mode="$2"
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh pom-bw "$2" "$1")"
}

# ------------------------------------------------------------------------------
# Usage: pom_bw PATH [--transitive]
#
# Get all dependencies from pom in PATH and print the Black/white list status
# ------------------------------------------------------------------------------
pom_bw() {
    local path="$1"
    local mode="$2"
    echo "$(${SCRIPT_DIR}/da-cli.sh pom-bw "$2" "$1")"
}

# ------------------------------------------------------------------------------
# Usage: pom_report_admin PATH [modes]
#
# modes can be "--transitive" and/or "--raw"
#
# Check all dependencies from pom in PATH and print the status report
# ------------------------------------------------------------------------------
pom_report_admin() {
    local path="$1"
    local modes="$2"
    echo "$(${SCRIPT_DIR}/da-cli-admin.sh pom-report "$2" "$1")"
}

# ------------------------------------------------------------------------------
# Usage: pom_report PATH [--transitive]
#
# Get all dependencies from pom in PATH and print the Black/white list status
# ------------------------------------------------------------------------------
pom_report() {
    local path="$1"
    local mode="$2"
    echo "$(${SCRIPT_DIR}/da-cli.sh pom-report "$2" "$1")"
}
