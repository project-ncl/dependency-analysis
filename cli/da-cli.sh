#!/bin/bash

basedir=`dirname $0`
listings="$basedir/listings.sh"

. $listings

printUsage() {
    echo ""
    echo "$0 check (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black or white list."
    echo ""
    echo "$0 list (b[lack]|w[hite])"
    echo "    List all artifacts in black or white list."
    echo ""
    echo "$0 lookup [GROUP_ID:ARTIFACT_ID:VERSION]";
    echo "    When GROUP_ID:ARTIFACT_ID:VERSION is specified finds corresponding redhat versions for it."
    echo "    When it is not specified, reads G:A:Vs from standard input and finds corresponding redhat versions for all of them."
    echo "    Output: <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    echo "$0 report [--raw|--json] GROUP_ID:ARTIFACT_ID:VERSION";
    echo "    Generate dependency report for GROUP_ID:ARTIFACT_ID:VERSION."
    echo "    Output: <Tree of groupId:artifactId:version> <Best Matched Red Hat Version> <In black/white list?> <Number of not built dependencies> <Number of available versions>"
    echo "    --raw output: <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Number of not built dependencies> <Available Versions>"
    echo "    --json output: json aquiered from server"
    echo ""
    echo "$0 pom-bw [--transitive] [path]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status."
    echo ""
    echo "$0 pom-bw-junit-xml [--transitive] [path]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status, and generate a JUnit XML file"
    echo ""
    echo "$0 scm-report [--raw|--json] scm tag pom-path";
    echo "    Check all dependencies from git-scm link"
    echo "    Output: "
    echo "    <groupId>:<artifactId>:<version> ::"
    echo "      <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    echo "$0 scm-report-advanced [--json] scm tag pom-path";
    echo "    Check all dependencies from git-scm link and print sumrarized information"
    echo "    Output: "
    echo "    Blacklisted artifacts: <groupId>:<artifactId>:<version>..."
    echo "    Whitelisted artifacts: <groupId>:<artifactId>:<version>..."
    echo "    Built community artifacts: <groupId>:<artifactId>:<version>..."
    echo "    Community artifacts with other built version: <groupId>:<artifactId>:<version>..."
    echo "    Community artifacts: <groupId>:<artifactId>:<version>..."
    echo "    <groupId>:<artifactId>:<version> ::"
    echo "      <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    echo "DEPRECATED:"
    echo "$0 pom-report";
    echo "    This option was deprecated, use scm-report instead."
    echo ""
    exit
}


if [ $# -lt 1 ]; then
    printUsage
    exit
fi

action=$1

case $action in
    check) check $2 $3;;
    list) list $2;;
    pom-bw) pom_bw $2 $3;;
    pom-bw-junit-xml) pom_bw_junit_xml $2 $3;;
    pom-report) echo "This option was deprecated, use scm-report instead." ;;
    scm-report) scm_report $2 $3 $4 $5;;
    scm-report-advanced) scm_report_adv $2 $3 $4 $5;;
    lookup) lookup $2;;
    report) report $2 $3;;
    *) printUsage ;;
esac

