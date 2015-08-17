#!/bin/bash

basedir=`dirname $0`
listings="$basedir/listings.sh"

. $listings

printUsage() {
    echo ""
    echo "$0 check (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black or white list"
    echo ""
    echo "$0 list (b[lack]|w[hite])"
    echo "    List all artifacts in black or white list"
    echo ""
    echo "$0 pom-bw [--transitive]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status"
    echo ""
    echo "$0 pom-report [--transitive]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their report status"
    echo "    Output: <groupId>:<artifactId>:<version> :: <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <Available Versions> <In black/white list?>"
    echo ""
    echo "$0 lookup";
    echo "    Read G:A:Vs from standard input and finds corresponding redhat versions"
    echo ""
    echo "$0 report GROUP_ID:ARTIFACT_ID:VERSION";
    echo "    Generate dependency report for GROUP_ID:ARTIFACT_ID:VERSION"
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
    pom-bw) pom_bw $2;;
    pom-report) pom_report $2;;
    lookup) lookup;;
    report) report $2;;
    *) printUsage ;;
esac

