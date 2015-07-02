#!/bin/bash

target=localhost:8080/reports-rest/rest/listings

testSuccess() {
    echo '{
        "success":true
    }'
}

testContains() {
    echo '{
        "contains":true
    }'
}

testNotContains() {
    echo '{
        "contains":false
    }'
}


printUsage() {
    echo "$0 (add|check) (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "$0 list (b[lack]|w[hite])"
    exit
}

pretyprintGAV() {
    python -m json.tool | egrep '"(artifact_id|group_id|version)"' | sed -r 's/ *"(artifact_id|group_id)": "([^"]*)",?/\2:/; s/ *"version": "([^"]*)",?/\1~/;' | tr -d "\n" | tr "~" "\n"
}

list() {
    curl -s -H "Content-Type: application/json" -X GET $target/${color}list | pretyprintGAV
}

matchGAV() {
    [ -z $1 ] && printUsage
    if ! echo $1 | grep -q "^[^:]\+:[^:]\+:[^:]\+$"; then
        echo "the GAV is not in format GROUP_ID:ARTIFACT_ID:VERSION"
        exit 1
    fi
    groupId=`echo $1 | cut -f1 -d:`
    artifactId=`echo $1 | cut -f2 -d:`
    version=`echo $1 | cut -f3 -d:`
}

add() {
    matchGAV $1
    tmpfile=`mktemp`
    curl -s -H "Content-Type: application/json" -X POST -d '{"group_id":"'${groupId}'", "artifact_id":"'${artifactId}'", "version":"'${version}'"}' "$target/$color" > $tmpfile
    if ! grep -q '"success":true' $tmpfile; then
        echo "Error adding $groupId:$artifactId:$version"
        cat $tmpfile
        echo
    fi
    rm $tmpfile
}

check() {
    matchGAV $1
    tmpfile=`mktemp`
    curl -s -H "Content-Type: application/json" -X GET "$target/$color?groupid=${groupId}&artifactid=${artifactId}&version=${version}" > $tmpfile
    if grep -q '"contains":true' $tmpfile; then
        echo "Artifact $groupId:$artifactId:$version is ${color}listed"
    elif grep -q '"contains":false' $tmpfile; then
        echo "Artifact $groupId:$artifactId:$version is NOT ${color}listed"
    else
        echo "Error checking $groupId:$artifactId:$version"
        cat $tmpfile
        echo
    fi
    rm $tmpfile
}

if [ $# -lt 2 ]; then
    printUsage
    exit
fi

action=$1
color=$2

case $color in
    w) color=white ;;
    b) color=black ;;
    white|black) ;;
    *) printUsage ;;
esac

case $action in
    add) add $3;;
    check) check $3;;
    list) list;;
    *) printUsage ;;
esac

