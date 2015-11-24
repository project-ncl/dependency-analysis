#!/bin/bash

BASEDIR="$(cd `dirname "${BASH_SOURCE[0]}"` && pwd)"
SCRIPT_DIR="${BASEDIR}/../cli"

export DA_TEST_SERVER="localhost:8080/da/rest/v-0.3"

. ${BASEDIR}/da-cli-test-helper.sh

assert_string_not_empty() {
    # used for printing the ok or failed status
    local bold_txt_green='\e[1;32m'
    local bold_txt_red='\e[1;31m'
    local color_off='\e[0m'

    local output="$1"

    if ! [[ -z "$output" ]]; then
        echo -e " ${bold_txt_green}[OK]${color_off}"
    else
        echo -e " ${bold_txt_red}[FAILED]${color_off}"
        echo "> "
        echo -e "> --- Actual output is empty ---"
        echo ""
    fi

}


# ------------------------------------------------------------------------------
# Usage: assert_string_equal EXPECTED ACTUAL
# ------------------------------------------------------------------------------
assert_string_equal() {
    # used for printing the ok or failed status
    local bold_txt_green='\e[1;32m'
    local bold_txt_red='\e[1;31m'
    local color_off='\e[0m'

    local expected_output="$1"
    local output="$2"

    if [[ "$output" == "${expected_output}" ]]; then
        echo -e " ${bold_txt_green}[OK]${color_off}"
    else
        echo -e " ${bold_txt_red}[FAILED]${color_off}"
        echo "> "
        echo -e "> --- Expected ---"
        echo -e "${expected_output}"
        echo ""
        echo -e "> ---  Actual  ---"
        echo -e "${output}"
        echo ""
    fi

}

test_should_not_add_non_redhat_version_to_whitelist() {
    echo -n "> Should not be able to add non-redhat versions to whitelist"

    local expected_output=$(cat <<EOL
Error adding test:testy:1.0.0
{"message":"Can't add artifact to whitelist, artifact is not in redhat version."}
EOL
)

    local output="$(add_list_admin white test:testy:1.0.0)"
    assert_string_equal "${expected_output}" "${output}"
}

test_whitelist() {
    test_add_check_delete_to_list white test:testy:1.0.1-redhat-1 1.0.1-redhat-1
}

test_blacklist() {
    test_add_check_delete_to_list black haha:hoho:1.0.2 1.0.2
}

# ------------------------------------------------------------------------------
# Usage: test_add_check_delete_to_list [white|black] G:A:V V
# ------------------------------------------------------------------------------
test_add_check_delete_to_list() {

    local list_type="$1"
    local artifact="$2"
    local version="$3"

    echo "--- ${list_type}list tests ---"
    echo ""
    # delete the artifact from the ${list_type}list, just in case it is there
    delete_list_admin ${list_type} ${artifact} > /dev/null

    echo -n "> ${list_type}list should not contain GAV ${artifact}"
    local expected_gav_not_in_list="Artifact ${artifact} is NOT ${list_type}listed"
    local gav_not_in_list=$(check_list_admin ${list_type} ${artifact})
    assert_string_equal "${expected_gav_not_in_list}" "${gav_not_in_list}"

    # now that we know {$list_type}list does not contain the gav, let's add it
    echo -n "> Should be able to add ${artifact} to ${list_type}list"
    local add_gav_list=$(add_list_admin ${list_type} "${artifact}")
    local expected_add_gav_list=""
    assert_string_equal "${expected_add_gav_list}" "${add_gav_list}"

    # make sure we cannot add a duplicate gav
    echo -n "> Should not be able to add same gav ${artifact} to ${list_type}list"
    local add_gav_duplicate=$(add_list_admin ${list_type} "${artifact}")
    local expected_add_gav_duplicate=$(cat <<EOL
Error adding ${artifact}
{"success":false}
EOL
)
    assert_string_equal "${expected_add_gav_duplicate}" "${add_gav_duplicate}"

    # use check to verify gav in ${list_type}list
    echo -n "> GAV ${artifact} should be marked as ${list_type}listed when using the 'check' subcommand"
    local check_gav_list=$(check_list_admin ${list_type} ${artifact})
    local expected_check_gav_list="Artifact ${artifact} is ${list_type}listed - actual verisions in list: ${version}"
    assert_string_equal "${expected_check_gav_list}" "${check_gav_list}"

    echo -n "> Check if output of 'check' subcommand for cli and cli-admin script are the same"
    local check_gav_list_nonadmin=$(check_list ${list_type} ${artifact})
    assert_string_equal "${check_gav_list}" "${check_gav_list_nonadmin}"

    # use list to verify gav in ${list_type}list
    echo -n "> GAV ${artifact} should be marked as ${list_type}listed when using the 'list' subcommand"
    local list_gav=$(list_admin ${list_type} | grep "${artifact}")
    assert_string_equal "${artifact}" "${list_gav}"

    echo -n "> Check if output of 'list' subcommand for cli and cli-admin script are the same"
    local list_gav_nonadmin=$(list ${list_type} | grep "${artifact}")
    assert_string_equal "${list_gav}" "${list_gav_nonadmin}"

    # delete gav
    echo -n "> Check if we can delete gav ${artifact}"
    local delete_gav_list=$(delete_list_admin ${list_type} ${artifact})
    local expected_delete_gav_list=""
    assert_string_equal "${expected_delete_gav_list}" "${delete_gav_list}"

    # verify if the gav is really deleted
    echo -n "> Deleted gav ${artifact} should not show up in check for admin script"
    local check_gav_list_delete=$(check_list_admin ${list_type} ${artifact})
    assert_string_equal "${expected_gav_not_in_list}" "${check_gav_list_delete}"

    # verify if the gav is really deleted
    echo -n "> Deleted gav ${artifact} should not show up in check for normal script"
    local check_gav_list_delete_non_admin=$(check_list ${list_type} ${artifact})
    assert_string_equal "${expected_gav_not_in_list}" "${check_gav_list_delete_non_admin}"

    echo -n "> Deleted gav ${artifact} should not show up in list for admin script"
    local list_gav_delete=$(list_admin ${list_type} | grep "${artifact}")
    local expected_list_gav_delete=""
    assert_string_equal "${expected_list_gav_delete}" ${list_gav_delete}

    echo -n "> Deleted gav ${artifact} should not show up in list for normal script"
    local list_gav_delete_non_admin=$(list ${list_type} | grep "${artifact}")
    assert_string_equal "${expected_list_gav_delete}" ${list_gav_delete_non_admin}
    echo ""
}

test_add_blacklist_redhat_version_removed() {
    echo "--- test add to blacklist will remove redhat version ---"

    local artifact="haha:hihi:1.0.1.redhat-1"
    local artifact_nonredhat="haha:hihi:1.0.1"

    local artifact2="test:test:2.1.3-redhat-1"
    local artifact2_nonredhat="test:test:2.1.3"

    # make sure both gavs are not in the blacklist
    delete_list_admin black ${artifact_nonredhat} > /dev/null
    delete_list_admin black ${artifact2_nonredhat} > /dev/null

    add_list_admin black ${artifact} > /dev/null

    add_list_admin black ${artifact2} > /dev/null

    local list_black=$(list_admin black | grep "haha:hihi")
    local list_black2=$(list_admin black | grep "test:test")

    echo -n "> When adding GAV ${artifact} to blacklist, the redhat version should be removed"
    assert_string_equal "${artifact_nonredhat}" "${list_black}"

    echo -n "> When adding GAV ${artifact2} to blacklist, the redhat version should be removed"
    assert_string_equal "${artifact2_nonredhat}" "${list_black2}"

    # cleanup
    delete_list_admin black ${artifact_nonredhat} > /dev/null
    delete_list_admin black ${artifact2_nonredhat} > /dev/null

    echo ""
}


test_migrate_from_whitelist_to_blacklist() {
    echo "--- Test migration from whitelist to blacklist ---"

    local artifact="hihi:hoho:2.8.1-redhat-1"
    local artifact_nonredhat="hihi:hoho:2.8.1"

    # cleanup
    delete_list_admin white "${artifact}" > /dev/null
    delete_list_admin black "${artifact_nonredhat}" > /dev/null

    add_list_admin white "${artifact}" > /dev/null
    local add_blacklist=$(add_list_admin black "${artifact_nonredhat}")
    local expected_add_blacklist="\"message\":\"Artifact was moved from whitelist into blacklist\""
    echo -n "> Testing for migration of gav from whitelist to blacklist"
    assert_string_equal "${expected_add_blacklist}" "${add_blacklist}"

    echo -n "> Make sure migrated gav ${artifact} not in whitelist"
    local in_whitelist=$(list_admin white | grep ${artifact})
    assert_string_equal "" "${in_blacklist}"

    echo -n "> Make sure migrated gav ${artifact} in blacklist"
    local in_blacklist=$(list_admin black | grep ${artifact_nonredhat})
    assert_string_equal "${artifact_nonredhat}" "${in_blacklist}"

    # cleanup
    delete_list_admin black "${artifact_nonredhat}" > /dev/null

    echo ""
}

test_no_migrate_from_blacklist_to_whitelist() {
    echo "--- Test no migration from blacklist to whitelist ---"

    local artifact_nonredhat="hihi:hoho:2.8.1"
    local artifact="hihi:hoho:2.8.1-redhat-1"

    delete_list_admin black "${artifact_nonredhat}" > /dev/null
    add_list_admin black "${artifact}" > /dev/null

    local add_whitelist=$(add_list_admin white ${artifact})
    local expected_add_whitelist=$(cat <<EOL
Error adding hihi:hoho:2.8.1-redhat-1
{"message":"Can't add artifact to whitelist, artifact is blacklisted"}
EOL
)

    echo -n "> blacklist should not be able to migrate to whitelist"
    assert_string_equal "${expected_add_whitelist}" "${add_whitelist}"

    local list_whitelist=$(list_admin white | grep ${artifact})
    echo -n "> artifact should not be in whitelist"
    assert_string_equal "" "${list_whitelist}"

    echo -n "> artifact should still be in blacklist"
    local list_blacklist=$(list_admin black | grep ${artifact_nonredhat})
    assert_string_equal "${artifact_nonredhat}" "${list_blacklist}"

    # cleanup
    delete_list_admin black "${artifact_nonredhat}" > /dev/null
    echo ""
}

test_lookup_gav() {
    echo "--- Test lookup gav ---"
    local artifact="xom:xom:1.2.5"
    local lookup_artifact="$(lookup_admin ${artifact})"

    local expected_lookup_artifact=$(echo -e "xom:xom:1.2.5\tNone\tno list\t1.2.7-redhat-1,1.2.7-redhat-3,1.2.7.redhat-4")
    echo -n "> Test lookup gav for ${artifact}"
    assert_string_equal "${expected_lookup_artifact}" "${lookup_artifact}"


    local lookup_artifact_non_admin="$(lookup ${artifact})"
    echo -n "> lookup admin output should be the same as non-admin"
    assert_string_equal "${lookup_artifact}" "${lookup_artifact_non_admin}"

    echo ""
}

test_report_gav() {
    set -e

    echo "--- Test report gav ---"
    local artifact="xom:xom:1.2.5"
    local report_artifact="$(report_admin ${artifact})"
    echo -n "> test that report output is not empty"
    assert_string_not_empty "${report_artifact}"

    echo -n "> test that report non-admin output is not empty"
    local report_artifact_non_admin="$(report ${artifact})"
    assert_string_not_empty "${report_artifact_non_admin}"
    echo ""
    set + e
}

test_pom_bw() {
    set -e

    echo "--- Test pom bw ---"
    local temp_dir=$(mktemp -d)

    pushd "${temp_dir}" > /dev/null
    echo "Cloning repository..."
    git clone https://github.com/project-ncl/dependency-analysis.git > /dev/null 2>&1
    cd dependency-analysis
    git checkout 0.3.0 > /dev/null 2>&1
    local pom_bw_output="$(pom_bw_admin .)"
    local pom_bw_output_non_admin="$(pom_bw .)"
    popd > /dev/null

    echo -n "> test that pom-bw output is not empty"
    assert_string_not_empty "${pom_bw_output}"

    echo -n "> test that pom-bw non-admin output is not empty"
    assert_string_not_empty "${pom_bw_output_non_admin}"

    rm -rf "${temp_dir}"
    echo ""
    set + e
}

test_pom_report() {
    set -e

    echo "--- Test pom report ---"
    local temp_dir=$(mktemp -d)

    pushd "${temp_dir}" > /dev/null
    echo "Cloning repository..."
    git clone https://github.com/project-ncl/dependency-analysis.git > /dev/null 2>&1
    cd dependency-analysis
    git checkout 0.3.0 > /dev/null 2>&1
    local pom_report_output="$(pom_report_admin .)"
    local pom_report_output_non_admin="$(pom_report .)"
    popd > /dev/null

    echo -n "> test that pom-report output is not empty"
    assert_string_not_empty "${pom_report_output}"

    echo -n "> test that pom-report non-admin output is not empty"
    assert_string_not_empty "${pom_report_output_non_admin}"

    rm -rf "${temp_dir}"
    echo ""
    set + e
}

test_scm_report() {
    set -e

    echo "--- Test report scm ---"
    local scm="https://github.com/project-ncl/dependency-analysis.git"
    local tag="master"
    local path="pom.xml"
    echo -n "> test that scm report output is not empty"
    report_scm="$(scm_report "${scm}" "${tag}" "${path}")"
    assert_string_not_empty "${report_scm}"

    echo -n "> test that report non-admin output is not empty"
    local report_scm_non_admin="$(scm_report "${scm}" "${tag}" "${path}")"
    assert_string_not_empty "${report_scm_non_admin}"
    echo ""
    set +e
}

# ==============================================================================
# Tests to run
# ==============================================================================
test_whitelist
test_blacklist
test_add_blacklist_redhat_version_removed
test_migrate_from_whitelist_to_blacklist
test_no_migrate_from_blacklist_to_whitelist
test_lookup_gav
test_report_gav
test_pom_bw
test_pom_report
test_scm_report
