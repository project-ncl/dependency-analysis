#!/bin/bash

target=ncl-test-vm-01.host.prod.eng.bos.redhat.com:8180/da/rest/v-0.2
target=localhost:8080/da/rest/v-0.2

basedir=`dirname $0`

setColor() {
    case $1 in
        w) color=white ;;
        b) color=black ;;
        white|black) color=$1;;
        *) printUsage; exit ;;
    esac
}

prettyPrintGAV() {
    python -m json.tool | \
        egrep '"(artifactId|groupId|version)"' | \
        sed -r 's/ *"groupId": "([^"]*)",?/g:\1\t/;
                s/ *"artifactId": "([^"]*)",?/a:\1\t/;
                s/ *"version": "([^"]*)",?/v:\1\t~/;' | \
        tr -d "\n" | tr "~" "\n" | \
        sed -r 's/(g:([^\t]*)\t|a:([^\t]*)\t|v:([^\t]*)\t)*/\2:\3:\4/'
}

prettyPrint() {
    pushd "${basedir}" > /dev/null
    python -c "import pretty; pretty.$1()"
    popd > /dev/null
}

get() {
    curl -s -H "Content-Type: application/json" -X GET "$target/$1"
}

post() {
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
    get "listings/${color}list" | prettyPrintGAV
}

deleteGAVFromList() {
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
    elif grep -q '"message":' $tmpfile; then
        grep -o '"message":"[^"]*"' $tmpfile
    fi
    rm $tmpfile
}

check() {
    setColor $1
    matchGAV $2
    tmpfile=`mktemp`
    get "listings/${color}list/gav?groupid=${groupId}&artifactid=${artifactId}&version=${version}" > $tmpfile
    if grep -q '"contains":true' $tmpfile; then
        echo -n "Artifact $groupId:$artifactId:$version is ${color}listed - actual verisions in list: "
        cat $tmpfile | prettyPrint ${color}listCheck
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
    type="pretty"
    if [ "X$1" = "X--raw" ]; then
        type="raw"
        shift
    elif [ "X$1" = "X--json" ]; then
        type="json"
        shift
    fi
    matchGAV $1
    tmpfile=`mktemp`
    post "reports/gav" "`formatGAVjson`" >> $tmpfile
    case $type in
        pretty) cat $tmpfile | prettyPrint report | column -t -s $'\t' ;;
        raw) cat $tmpfile | prettyPrint reportRaw ;;
        json) cat $tmpfile ;;
    esac
    rm $tmpfile
}

lookup() {
    local query="["
    if [ $# -ge 1 ]; then # G:A:V specified on command line
        matchGAV $1
        query="$query `formatGAVjson`"
    else                  # G:A:Vs specified in standart input
        local first=true
        while read line; do
            matchGAV $line
            $first || query="$query,"
            first=false
            query="$query `formatGAVjson`"
        done
    fi
    query="$query ]"
    tmpfile=`mktemp`
    post "reports/lookup/gavs" "$query" > $tmpfile
    cat $tmpfile | prettyPrint lookup
    rm $tmpfile
}

parse_pom_bw_report_options() {

    pom_transitive_flag=false;

    local wrong_option=""

    for key in "$@"
    do
        case ${key} in
            --transitive) pom_transitive_flag=true;;
            --raw)        raw_output="--raw";;
            -*)           wrong_option="${key}";;
            *)            pom_path="${key}";;
        esac
    done

    # set a default value for pom_path if not specified by user
    if [ -z "${pom_path}" ]; then
        pom_path=$(pwd)
    fi

    # if wrong flag passed
    if ! [ -z "${wrong_option}" ]; then
        echo ""
        echo "Wrong option: '${wrong_option}' specified. Aborting"
        exit 1
    fi

    # if path does not exist
    if ! [ -d "${pom_path}" ]; then
        echo ""
        echo "The directory '${pom_path}' does not exist! Aborting"
        exit 1
    fi

    # if path does not contain the pom.xml file
    if ! [ -f "${pom_path}/pom.xml" ]; then
        echo ""
        echo "No pom.xml file present in the directory '${pom_path}'. Aborting"
        exit 1
    fi
}

pom_bw() {

    parse_pom_bw_report_options "$@"

    mvn_opts=""
    if [ ${pom_transitive_flag} = true ]; then
        mvn_opts="$mvn_opts"
    else
        mvn_opts="$mvn_opts -DexcludeTransitive=true"
    fi

    if [ -t 1 ]; then
        RED="$(tput setaf 1)"
        GREEN="$(tput setaf 2)"
        YELLOW="$(tput setaf 3)"
        DEFAULT="$(tput sgr0)"
    fi

    tmpfile=`mktemp`
    pushd "${pom_path}" > /dev/null
    mvn -q dependency:list -DoutputFile=$tmpfile -DappendOutput=true $mvn_opts
    popd > /dev/null

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

pom_report() {

    parse_pom_bw_report_options "$@"

    mvn_opts=""
    if [ ${pom_transitive_flag} = true ]; then
        mvn_opts="$mvn_opts"
    else
        mvn_opts="$mvn_opts -DexcludeTransitive=true"
    fi

    tmpfile=`mktemp`

    pushd "${pom_path}" > /dev/null
    mvn -q dependency:list -DoutputFile=$tmpfile -DappendOutput=true $mvn_opts
    popd > /dev/null

    if [ $? -ne 0 ]; then
        rm $tmpfile
        exit
    fi

    sort -u $tmpfile | grep "^ *.*:.*:.*:.*"| sed "s/^ *//" | awk 'BEGIN {IFS=":"; FS=":"; OFS=":"} {print $1,$2,$4}' | while read line; do
        report_result=`report $raw_output $line`
        echo "$line ::"
        echo "$report_result" | sed "s/^/  /"
    done
    echo -n "$DEFAULT"
    rm $tmpfile
}
