#!/bin/bash

target=10.3.10.85:8080/da/rest/v-0.2

basedir=`dirname $0`

setColor() {
    case $1 in
        w) color=white ;;
        b) color=black ;;
        white|black) color=$1;;
        *) printUsage; exit ;;
    esac
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

get() {
    curl -s -H "Content-Type: application/json" -X GET "$target/$1"
}

post() {
    echo curl -s -H "Content-Type: application/json" -X POST -d "$2" "$target/$1" >&2
    curl -s -H "Content-Type: application/json" -X POST -d "$2" "$target/$1"
}

delete() {
    curl -s -H "Content-Type: application/json" -X DELETE -d "$2" "$target/$1"
}

matchGAV() {
    [ -z $1 ] && printUsage
    if ! echo $1 | grep -q "^[^:]\+:[^:]\+:[^:]\+$"; then
        echo "the GAV is not in format GROUP_ID:ARTIFACT_ID:VERSION: $1"
        exit 1
    fi
    groupId=`echo $1 | cut -f1 -d:`
    artifactId=`echo $1 | cut -f2 -d:`
    version=`echo $1 | cut -f3 -d:`
}

formatGAVjson() {
    echo '{"groupId":"'${groupId}'", "artifactId":"'${artifactId}'", "version":"'${version}'"'$1'}'
}

list() {
    setColor $1
    get "listings/${color}list" | pretyprintGAV
}

delete() {
    setColor $1
    matchGAV $2
    tmpfile=`mktemp`
    delete "listings/${color}list/gav" "`formatGAVjson`" > $tmpfile
    if ! grep -q '"success":true' $tmpfile; then
        echo "Error removing $groupId:$artifactId:$version"
        cat $tmpfile
        echo
    fi
    rm $tmpfile
}

add() {
    setColor $1
    matchGAV $2
    tmpfile=`mktemp`
    post "listings/${color}list/gav" "`formatGAVjson`" > $tmpfile
    if ! grep -q '"success":true' $tmpfile; then
        echo "Error adding $groupId:$artifactId:$version"
        cat $tmpfile
        echo
    fi
    rm $tmpfile
}

check() {
    setColor $1
    matchGAV $2
    tmpfile=`mktemp`
    get "listings/${color}list/gav?groupid=${groupId}&artifactid=${artifactId}&version=${version}" > $tmpfile
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

report() {
    matchGAV $1
    echo `formatGAVjson ', "products":[]'`
    tmpfile=`mktemp`
    echo "[" > $tmpfile # WA until there is pretty print for reports
    post "reports/gav" "`formatGAVjson ', "products":[]'`" >> $tmpfile
    echo "]" >> $tmpfile # WA until there is pretty print for reports
    cat $tmpfile | $basedir/pretty-lookup.py
    rm $tmpfile
}

lookup() {
    local query="["
    local first=true
    while read line; do
        matchGAV $line
        $first || query="$query,"
        first=false
        query="$query `formatGAVjson`"
    done
    query="$query ]"
    tmpfile=`mktemp`
    curl -s -H "Content-Type: application/json" -X POST -d "$query" "$target/reports/lookup/gav" > $tmpfile
    cat $tmpfile | $basedir/pretty-lookup.py
    rm $tmpfile
}

pom() {
    mvn_opts=""
    if [ "x$1" = "x--no-transitive" ]; then
        mvn_opts="$mvn_opts -DexcludeTransitive=true"
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
        wresp=`check white $line`
        bresp=`check black $line`
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
}
