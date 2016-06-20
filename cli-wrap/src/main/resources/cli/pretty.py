#!/usr/bin/python
import json
import sys
import fileinput


def formatProduct(product, supportStatus=True):
    if supportStatus:
        return product["name"] + ":" + product["version"] + " " + product["supportStatus"]
    else:
        return product["name"] + ":" + product["version"]

def formatVersionProduct(vp):
    return vp['version'] + " (" + formatProduct(vp['product'], False) + ")" 

def getBestSupportStatus(products):
    status = "UNKNOWN"
    for product in products:
        ps = product["supportStatus"]
        if (  (status == "UNKNOWN" and ps in {"UNSUPPORTED", "SUPERSEDED", "SUPPORTED"})
           or (status == "UNSUPPORTED" and ps in {"SUPERSEDED", "SUPPORTED"})
           or (status == "SUPERSEDED" and ps in {"SUPPORTED"}) ):
            status = product["supportStatus"]
    return status

def getList(artifact, raw):
    if artifact["blacklisted"] and artifact["whitelisted"]:
        return "both lists"
    elif artifact["blacklisted"]:
        return "blacklisted"
    elif artifact["whitelisted"]:
        products = artifact["whitelisted"]
        if raw:
            return ",".join(map(formatProduct, products))
        else:
            return "whitelisted (" + str(len(products)) + ", " + getBestSupportStatus(products) + ")"
    else:
        return "graylisted"

def getAvailableVersions(artifact, raw):
    if "availableVersions" in artifact:
        if raw:
            return ",".join(artifact["availableVersions"])
        else:
            return str(len(artifact["availableVersions"])) + " available versions"
    else:
        return ""

def getGAV(artifact):
    return artifact["groupId"]+":"+artifact["artifactId"]+":"+artifact["version"]

def getGAVList(artifacts):
    return ", ".join(map(getGAV,artifacts))

def getDependencyVersionsSatisfied(artifact):
    if artifact["dependencyVersionsSatisfied"]:
        return "all dependencies built"
    else:
        return str(artifact["notBuiltDependencies"]) + " dependencies not built"

def printReport(artifact, depth = None):
    lists = getList(artifact, depth is None)
    versions = getAvailableVersions(artifact, depth is None)
    gav = getGAV(artifact)
    bestMatch = str(artifact["bestMatchVersion"])
    depsSatisfied = getDependencyVersionsSatisfied(artifact)

    if depth is None:
        indent = ""
    else:
        indent = "| " * (depth -1)
        if depth > 0:
            indent += "+-"

    print indent + gav + "\t" + bestMatch + "\t" + lists + "\t" + depsSatisfied +  "\t" + versions

    for dep in artifact["dependencies"]:
        if depth is None:
            printReport(dep)
        else:
            printReport(dep, depth + 1)

def printReportAdvSum(data):
    print "Blacklisted artifacts:\t" + getGAVList(data["blacklistedArtifacts"])

    print "Whitelisted artifacts:"
    for whitelist in data["whitelistedArtifacts"]:
        print "  " + getGAV(whitelist) + "\t" + ", ".join(map(formatProduct, whitelist["products"]))

    print "Built community artifacts:"
    for bestMatch in data["communityGavsWithBestMatchVersions"]:
        print "  " + getGAV(bestMatch) + "\t" + bestMatch["bestMatchVersion"]

    print "Community artifacts with other built version:"
    for builtVersion in data["communityGavsWithBuiltVersions"]:
        print "  " + getGAV(builtVersion) + "\t" +  ", ".join(builtVersion["availableVersions"])

    print "Community artifacts:\t" + getGAVList(data["communityGavs"])

def printReportAlign(data):
    print "Internaly built:"
    for module in data['internallyBuilt']:
        print "  " + module['groupId'] + ":" + module['artifactId']
        for dependency in module['gavProducts']:
            print "    " + getGAV(dependency) + " - " + ", ".join(map(formatVersionProduct,dependency['gavProducts']))
    print

    print "Built in different version:"
    for module in data['builtInDifferentVersion']:
        print "  " + module['groupId'] + ":" + module['artifactId']
        for dependency in module['gavProducts']:
            print "    " + getGAV(dependency) + " - " + ", ".join(map(formatVersionProduct,dependency['gavProducts']))
    print

    print "Not built:"
    for module in data['notBuilt']:
        print "  " + module['groupId'] + ":" + module['artifactId']
        for dependency in module['gavs']:
            print "    " + getGAV(dependency)
    print

    print "Blacklisted:"
    for module in data['blacklisted']:
        print "  " + module['groupId'] + ":" + module['artifactId']
        for dependency in module['gavs']:
            print "    " + getGAV(dependency)

def printLookup(artifact):
    gav = getGAV(artifact)
    bestMatch = str(artifact["bestMatchVersion"])
    lists = getList(artifact, True)
    versions = getAvailableVersions(artifact, True)

    print gav + "\t" + bestMatch + "\t" + lists + "\t" + versions

def readInput():
    data=""
    for line in fileinput.input():
        data += line
    return json.loads(data)

def checkError(data):
    if "message" in data:
        # message key present when there is an error
        print(data["message"])
        sys.exit(1)

def report():
    data = readInput()
    checkError(data)
    print "tree of GAVs\tExact Match Version\tBlack/White list\t# of not built dependencies\t# of available versions"
    printReport(data,0)

def reportRaw():
    data = readInput()
    checkError(data)
    printReport(data)

def reportAdv():
    data = readInput()
    checkError(data)
    print "tree of GAVs\tExact Match Version\tBlack/White list\t# of not built dependencies\t# of available versions"
    printReport(data["report"], 0)
    
def reportAdvSum():
    data = readInput()
    checkError(data)
    printReportAdvSum(data)

def reportAlign():
    data = readInput()
    checkError(data)
    printReportAlign(data)
    
def lookup():
    data = readInput()
    checkError(data)
    for artifact in data:
        printLookup(artifact)

def listCheck():
    data = readInput()
    versions = []
    for gav in data["found"]:
        versions.append(gav["version"])
    print ", ".join(versions)

def hello():
    print "hi"
