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
    echo "$0 add (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Add artifact GROUP_ID:ARTIFACT_ID:VERSION to black or white list"
    echo ""
    echo "$0 delete (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from black or white list"
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
    echo "$0 pom-report [--transitive] [--raw] [path]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their report status."
    echo "    Output: "
    echo "    <groupId>:<artifactId>:<version> ::"
    echo "      <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    exit
}


if [ $# -lt 1 ]; then
    printUsage
    exit
fi

action=$1

case $action in
    add) add $2 $3;;
    delete) deleteGAVFromList $2 $3;;
    check) check $2 $3;;
    list) list $2;;
    pom-bw) pom_bw $2 $3;;
    pom-report) pom_report $2 $3;;
    lookup) lookup $2;;
    report) report $2 $3;;
    *) printUsage ;;
esac

