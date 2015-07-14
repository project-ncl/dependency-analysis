#!/bin/bash

basedir=`dirname $0`
listings="$basedir/listings.sh"

mvn_opts=""
if [ "x$1" = "x--no-transitive" ]; then
    mvn_opts="$mvn_opts -DexcludeTransitive=true"
fi

if [ "x$1" = "x--help" -o "x$1" = "x-h" ]; then
    echo "$0 [--no-transitive]"
    echo "analyze pomfile in working directory for dependencies (using dependyncy:list) and print they Black/White list status"
    exit
fi


if [ -t 1 ]; then
    RED="$(tput setaf 1)"
    GREEN="$(tput setaf 2)"
    YELLOW="$(tput setaf 3)"
    DEFAULT="$(tput sgr0)"
fi


tmpfile=`mktemp`
mvn -q dependency:list -DoutputFile=$tmpfile -DappendOutput=true $mvn_opts
if [ $? -ne 0 ]; then
    rm $tmpfile
    exit
fi


sort -u $tmpfile | grep "^ *.*:.*:.*:.*"| sed "s/^ *//" | awk 'BEGIN {IFS=":"; FS=":"; OFS=":"} {print $1,$2,$4}' | while read line; do
    wresp=`$listings check w $line`
    bresp=`$listings check b $line`
    if echo $wresp | grep -q "is whitelisted"; then
        wl=true
    elif echo $wresp | grep -q "is NOT whitelisted"; then
        wl=false
    else
        echo "Error communicating with Black&White list service"
        break
    fi
    if echo $bresp | grep -q "is blacklisted"; then
        bl=true
    elif echo $bresp | grep -q "is NOT blacklisted"; then
        bl=false
    else
        echo "Error communicating with Black&White list service"
        break
    fi
    if $wl && $bl; then
        echo "${YELLOW}Both lists: $line"
    elif $wl; then
        echo "${GREEN}White list: $line"
    elif $bl; then
        echo "${RED}Black list: $line"
    else
        echo "${DEFAULT}None list:  $line"
    fi
done
echo -n "$DEFAULT"
rm $tmpfile

