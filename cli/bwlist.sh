#!/bin/bash

basedir=`dirname $0`
listings="$basedir/listings.sh"

. $listings

printUsage() {
    echo "$0 check (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "    Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black or white list"
    echo "$0 list (b[lack]|w[hite])"
    echo "    List all artifacts in black or white list"
    echo "$0 pom [--no-transitive]";
    echo "    Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status"
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
    pom) pom $2;;
    *) printUsage ;;
esac

