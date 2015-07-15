#!/bin/bash

target=10.3.10.85:8080/reports-rest/rest/listings

printUsage() {
    echo "$0 (add|delete|check) (b[lack]|w[hite]) GROUP_ID:ARTIFACT_ID:VERSION"
    echo "$0 list (b[lack]|w[hite])"
    exit
}

pretyprintGAV() {
    python -m json.tool | \
        egrep '"(artifactId|groupId|version)"' | \
        sed -r 's/ *"groupId": "([^"]*)",?/g:\1\t/;
                s/ *"artifactId": "([^"]*)",?/a:\1\t/;
                s/ *"version": "([^"]*)",?/v:\1\t~/;' | \
        tr -d "\n" | tr "~" "\n" | \
        sed -r 's/(g:([^\t]*)\t|a:([^\t]*)\t|v:([^\t]*)\t)*/\2:\3:\4/'
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

delete() {
    matchGAV $1
    tmpfile=`mktemp`
    curl -s -H "Content-Type: application/json" -X DELETE -d '{"groupId":"'${groupId}'", "artifactId":"'${artifactId}'", "version":"'${version}'"}' "$target/$color" > $tmpfile
    if ! grep -q '"success":true' $tmpfile; then
        echo "Error removing $groupId:$artifactId:$version"
        cat $tmpfile
        echo
    fi
    rm $tmpfile
}

add() {
    matchGAV $1
    tmpfile=`mktemp`
    curl -s -H "Content-Type: application/json" -X POST -d '{"groupId":"'${groupId}'", "artifactId":"'${artifactId}'", "version":"'${version}'"}' "$target/$color" > $tmpfile
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
    delete) delete $3;;
    check) check $3;;
    list) list;;
    *) printUsage ;;
esac

