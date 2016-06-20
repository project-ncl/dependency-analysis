#!/bin/bash

basedir=`dirname $0`
listings="$basedir/listings.sh"

. $listings

printUsage() {
    echo "DEPENDENCY ANALYZER CLI TOOL version @cli.version@"
    echo ""
    echo "    This CLI tool is used for communication with Dependency Analyzer, a service which provides information about built artifacts and analyse projects dependencies."
    echo "    This tool has two main usages: black & white lists of artifacts and dependency reports."
    echo ""
    echo "BLACK & WHITE LISTS"
    echo "    An artifact (groupId:artifactId:version) can be either whitelisted in some products, blacklisted in all product or graylisted."
    echo "    Each whitelisted artifact is whitelisted in one or more product versions. Each product can also be in one of the following states: supported, superseded, unsupported or unknown. Whitelisted artifacts are in their -redhat version."
    echo "    Blaclisted artifact is not associated with any product. When artifact is blacklisted, it is blacklisted across all products. Blacklisted artifacts are in their community versions."
    echo "    Graylisted artifacts are artifacts that are neither whitelisted nor blacklisted."
    echo ""
    echo "    $0 check b[lack] GROUP_ID:ARTIFACT_ID:VERSION"
    echo "        Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black list."
    echo ""
    echo "    $0 list black"
    echo "        List all artifacts in blacklist."
    echo ""
    echo "    $0 list white [PRODUCT_NAME:VERSION]"
    echo "        List all artifacts and its associated product in the white list."
    echo "        You can optionally limit which product version to show"
    echo "        You can optionally specify which product to show and it will show"
    echo "        all artifacts in that product."
    echo ""
    echo "    $0 list whitelist-products [GROUP_ID:ARTIFACT_ID:VERSION]"
    echo "        List all products in the white list"
    echo "        You can optionally list products which have a particular GAV"
    echo ""
    echo "    $0 list whitelist-ga GROUP_ID:ARTIFACT_ID STATUS"
    echo "        List all artifacts in the white list with a particular GA and status"
    echo "        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN"
    echo ""
    echo "    $0 list whitelist-gav GAV [STATUS...]"
    echo "        List all artifacts in the white list with a particular GAV,"
    echo "        and the product associated with the artifact."
    echo "        You can optionally specify the status(es) of the GAV"
    echo "        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN"
    echo ""
    echo "    $0 list whitelist-gavs STATUS"
    echo "        List all artifacts in the white list with a particular status"
    echo "        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN"
    echo ""
    echo "    $0 add black GROUP_ID:ARTIFACT_ID:VERSION"
    echo "        Add artifact GROUP_ID:ARTIFACT_ID:VERSION to blacklist"
    echo ""
    echo "    $0 add white GROUP_ID:ARTIFACT_ID:VERSION PRODUCT_NAME:VERSION"
    echo "        Add artifact GROUP_ID:ARTIFACT_ID:VERSION to white list for a particular product"
    echo ""
    echo "    $0 add whitelist-product PRODUCT_NAME:VERSION STATUS"
    echo "        Add whitelist-product and status"
    echo "        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN"
    echo ""
    echo "    $0 update whitelist-product PRODUCT_NAME:VERSION STATUS"
    echo "        Update whitelist-product with a particular status"
    echo "        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN"
    echo ""
    echo "    $0 delete black GROUP_ID:ARTIFACT_ID:VERSION"
    echo "        Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from black list"
    echo ""
    echo "    $0 delete white GROUP_ID:ARTIFACT_ID:VERSION [PRODUCT_NAME:VERSION]"
    echo "        Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from white list"
    echo "        Each artifact is associated to a product in the white list. You can delete all the artifacts with a particular GROUP_ID:ARTIFACT_ID:VERSION,"
    echo "        or delete artifacts with a particular GROUP_ID:ARTIFACT_ID:VERSION and PRODUCT_NAME:VERSION from the white list"
    echo ""
    echo "    $0 delete whitelist-product PRODUCT_NAME:VERSION"
    echo "        Delete product from white list"
    echo ""
    echo "    $0 pom-bw-junit-xml [--transitive] path [PRODUCT_NAME:VERSION]";
    echo "        Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status, and generate a JUnit XML file"
    echo "        If PRODUCT_NAME:VERSION is specified, the dependencies which are in the white list of the product will be considered as PASS; anything else will be considered as FAIL"
    echo ""
    echo "DEPENDENCY REPORTS"
    echo "    Dependency reports can be used to get detail information about artifact or project dependencies."
    echo ""
    echo "    $0 lookup [GROUP_ID:ARTIFACT_ID[:PACKAGING[:CLASSIFIER]]:VERSION[:SCOPE]]";
    echo "        When GROUP_ID:ARTIFACT_ID:VERSION is specified finds corresponding redhat versions for it."
    echo "        When it is not specified, reads G:A:Vs from standard input and finds corresponding redhat versions for all of them."
    echo "        Packaging, classifier and scope is ignored in both cases."
    echo "        Output: <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    echo "    $0 report [--raw|--json] GROUP_ID:ARTIFACT_ID:VERSION";
    echo "        Generate dependency report for GROUP_ID:ARTIFACT_ID:VERSION."
    echo "        Output: <Tree of groupId:artifactId:version> <Best Matched Red Hat Version> <In black/white list?> <Number of not built dependencies> <Number of available versions>"
    echo "        --raw output: <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Number of not built dependencies> <Available Versions>"
    echo "        --json output: json aquiered from server"
    echo ""
    echo "    $0 scm-report [--raw|--json] scm tag pom-path";
    echo "        Check all dependencies from git-scm link"
    echo "        Output: "
    echo "        <groupId>:<artifactId>:<version> ::"
    echo "          <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    echo "    $0 scm-report-advanced [--json] scm tag pom-path";
    echo "        Check all dependencies from git-scm link and print sumrarized information"
    echo "        Output: "
    echo "        Blacklisted artifacts: <groupId>:<artifactId>:<version>..."
    echo "        Whitelisted artifacts: <groupId>:<artifactId>:<version>..."
    echo "        Built community artifacts: <groupId>:<artifactId>:<version>..."
    echo "        Community artifacts with other built version: <groupId>:<artifactId>:<version>..."
    echo "        Community artifacts: <groupId>:<artifactId>:<version>..."
    echo "        <groupId>:<artifactId>:<version> ::"
    echo "          <groupId>:<artifactId>:<version> <Best Matched Red Hat Version> <In black/white list?> <Available Versions>"
    echo ""
    echo "    $0 align-report [--json] [--unknown] [--products PRODUCTS]... [--repository REPOSITORY]... SCM TAG [POM-PATH]"
    echo "        Check toplevel dependencies of all modules from git-scm link and print sumarized information."
    echo "          --json                    Output unparsed response from Dependency Analyzer."
    echo "          --unknown                 Consider artifacts from unknown products."
    echo "          --products PRODUCTS       Consider artifact only from specified products. PRODUCTS should be comma separated"
    echo "                                    list of product ids (you can obtain list of products using $0 list whitelist-products)."
    echo "          --repository REPOSITORY   Aditional maven repositories required by the analysed project. You can specify this"
    echo "                                    option multiple times."
    echo "        Output:"
    echo "        Internaly built:"
    echo "          MODULE"
    echo "              DEPENDENCY - BUILT_VERSION (PRODUCT)"
    echo "        Built in different version:"
    echo "          MODULE"
    echo "              DEPENDENCY - BUILT_VERSION (PRODUCT)"
    echo "        Not built:"
    echo "          MODULE"
    echo "              DEPENDENCY"
    echo "        Blacklisted:"
    echo "          MODULE"
    echo "              DEPENDENCY"
    echo ""
    echo "DEPRECATED"
    echo "    $0 pom-bw";
    echo "        This option was deprecated, use scm-report-advanced instead."
    echo ""
    echo "    $0 pom-report";
    echo "        This option was deprecated, use scm-report instead."
    echo ""
    exit
}


if [ $# -lt 1 ]; then
    printUsage
    exit
fi

action=$1

case $action in
    add) add $2 $3 $4;;
    delete) delete $2 $3 $4;;
    check) check $2 $3;;
    list) list $2 $3 $4;;
    update) update $2 $3 $4;;
    pom-bw) echo "This option was deprecated, use scm-report-advanced instead." ;;
    pom-bw-junit-xml) pom_bw_junit_xml $2 $3 $4;;
    pom-report) echo "This option was deprecated, use scm-report instead." ;;
    scm-report) scm_report $2 $3 $4 $5;;
    scm-report-advanced) scm_report_adv $2 $3 $4 $5;;
    lookup) lookup $2;;
    report) report $2 $3;;
    align-report) shift; scm_report_align "$@" ;;
    *) printUsage ;;
esac

