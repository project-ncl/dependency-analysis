#!/bin/bash
DA_MAIN_SERVER="ncl-test-vm-01.host.prod.eng.bos.redhat.com:8180/da/rest/v-0.3"
# DA_TEST_SERVER is defined in the da-cli-test.sh script
target="${DA_TEST_SERVER:-${DA_MAIN_SERVER}}"

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

gettmpfile() {
    mktemp 2>/dev/null || mktemp -t 'pncl'
}

get() {
    curl -s -H "Content-Type: application/json" -X GET "$target/$1"
}

post() {
    curl -s -H "Content-Type: application/json" -X POST -d "$2" "$target/$1"
}

delete() {
    case $1 in
        white)
            if [ -z "$3" ]; then
                ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} delete --white $2
            else
                ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} delete --white $2 --product $3
            fi
            ;;
        black) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} delete --black $2;;
        whitelist-product) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} delete --whitelist-product $2;;
        *) echo "I don't know this option"
    esac
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

    case $1 in
        white) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} list --white $2;;
        black) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} list --black;;
        whitelist-products) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} list --whitelist-products $2;;
        whitelist-ga) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} list --whitelist-ga $2 $3;;
        whitelist-gav) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} list --whitelist-gav $2 $3;;
        whitelist-gavs) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} list --whitelist-gavs $2;;
        *) echo "I don't know this option"
    esac
}

add() {
    case $1 in
        white) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} add --white $2 $3;;
        black) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} add --black $2;;
        whitelist-product) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} add --whitelist-product $2 $3;;
        *) echo "I don't know this option"
    esac

}

update() {
    case $1 in
        whitelist-product) ${basedir}/da-cli-script.py --server http://${DA_MAIN_SERVER} update --whitelist-product $2 $3;;
        *) echo "I don't know this option"
    esac
}

check() {
    setColor $1
    matchGAV $2
    tmpfile=`gettmpfile`
    get "listings/${color}list/gav?groupid=${groupId}&artifactid=${artifactId}&version=${version}" > $tmpfile
    if grep -q '"contains":true' $tmpfile; then
        echo -n "Artifact $groupId:$artifactId:$version is ${color}listed - actual verisions in list: "
        cat $tmpfile | prettyPrint listCheck
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
    tmpfile=`gettmpfile`
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
    tmpfile=`gettmpfile`
    post "reports/lookup/gavs" "$query" > $tmpfile
    cat $tmpfile | prettyPrint lookup
    rm $tmpfile
}

parse_pom_bw_report_options() {

    pom_transitive_flag=false;

    local wrong_option=""

    case $1 in
        --transitive) pom_transitive_flag=true;;
        -*)           wrong_option="${key}";;
        *)            pom_path="${key}";;
    esac

    # if wrong flag passed
    if ! [ -z "${wrong_option}" ]; then
        echo ""
        echo "Wrong option: '${wrong_option}' specified. Aborting"
        exit 1
    fi

    if [ ${pom_transitive_flag} == true ]; then
        pom_path="$2"
        prod_version="$3"
    else
        pom_path="$1"
        prod_version="$2"
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

pom_bw_junit_xml() {
    parse_pom_bw_report_options "$@"

    mvn_opts=""
    if [ ${pom_transitive_flag} = true ]; then
        mvn_opts="$mvn_opts"
    else
        mvn_opts="$mvn_opts -DexcludeTransitive=true"
    fi

    local tmpfile=`gettmpfile`
    pushd "${pom_path}" > /dev/null
    mvn -q dependency:list -DoutputFile=$tmpfile -DappendOutput=true $mvn_opts

    if [ $? -ne 0 ]; then
        rm $tmpfile
        echo ""
        echo ""
        echo "================================================================="
        echo "'mvn dependency:list' command failed."
        echo "Consider running 'mvn clean install' before running the pom-bw command again to fix the issue"
        echo "================================================================="
        exit
    fi

    popd > /dev/null

    local pkg_list_file=`gettmpfile`
    sort -u $tmpfile | grep "^ *.*:.*:.*:.*"| sed "s/^ *//" | awk 'BEGIN {IFS=":"; FS=":"; OFS=":"} {print $1,$2,$4}' | while read line; do
        echo "${line}" >> ${pkg_list_file}
    done

    python ${basedir}/testsuite.py ${pkg_list_file} ${prod_version}

    rm ${tmpfile}
    rm ${pkg_list_file}
}

scm_report() {
    local type="pretty"
    if [ "X$1" = "X--raw" ]; then
        type="raw"
        shift
    elif [ "X$1" = "X--json" ]; then
        type="json"
        shift
    fi

    local scm="$1"
    local tag="$2"
    local pom_path="$3"

    if [ -z "${scm}" ] || [ -z "${tag}" ] || [ -z "${pom_path}" ]; then
        echo "Error: You have to specify the scm, scm tag and the pom path to analyze"
        exit 1
    fi
    local scmJSON="{\"scmUrl\": \"${scm}\", \"revision\": \"${tag}\", \"pomPath\": \"${pom_path}\"}"

    local report="$(post "reports/scm" "${scmJSON}")"
    case $type in
      pretty) echo "$report" | prettyPrint report | column -t -s $'\t' ;;
      raw) echo "$report" | prettyPrint reportRaw ;;
      json) echo "$report" ;;
    esac
}

scm_report_adv() {
    local type="pretty"
    if [ "X$1" = "X--json" ]; then
        type="json"
        shift
    fi

    local scm="$1"
    local tag="$2"
    local pom_path="$3"

    if [ -z "$scm" ] || [ -z "$tag" ] || [ -z "$pom_path" ]; then
        echo "Error: You have to specify the scm, scm tag and the pom path to analyze"
        exit 1
    fi
    local scmJSON="{\"scmUrl\": \"${scm}\", \"revision\": \"${tag}\", \"pomPath\": \"${pom_path}\"}"

    tmpfile=`gettmpfile`
    post "reports/scm-advanced" "$scmJSON" >> $tmpfile
    case $type in
        pretty) cat $tmpfile | prettyPrint reportAdvSum | column -t -s $'\t'
                echo
                cat $tmpfile | prettyPrint reportAdv | column -t -s $'\t'
            ;;
        json) cat $tmpfile ;;
    esac
    rm $tmpfile
}


