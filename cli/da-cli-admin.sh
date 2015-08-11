#!/bin/bash

basedir=`dirname $0`
listings="$basedir/listings.sh"

. $listings

printUsage() {
    echo "$0 check (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black or white list"
    echo "$0 add (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Add artifact GROUP_ID:ARTIFACT_ID:VERSION to black or white list"
    echo "$0 delete (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from black or white list"
    echo "$0 list (b[lack]|w[hite])"
    echo "    List all artifacts in black or white list"
    echo "$0 pom [--no-transitive]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status"
    echo "$0 lookup";
    echo "    Read G:A:Vs from standard input and finds corresponding redhat versions"
    echo "$0 report GROUP_ID:ARTIFACT_ID:VERSION";
    echo "    Generate dependency report for GROUP_ID:ARTIFACT_ID:VERSION"
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
    pom) pom $2;;
    lookup) lookup;;
    report) report $2;;
    *) printUsage ;;
esac

