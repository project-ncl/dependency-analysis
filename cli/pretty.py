#!/usr/bin/python
import json
import sys
import fileinput


def getList(artifact):
    if artifact["blacklisted"] and artifact["whitelisted"]:
        return "both lists"
    elif artifact["blacklisted"]:
        return "black list"
    elif artifact["whitelisted"]:
        return "white list"
    else:
        return "no list"

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

def getDependencyVersionsSatisfied(artifact):
    if artifact["dependencyVersionsSatisfied"]:
        return "all dependencies built"
    else:
        return str(artifact["notBuiltDependencies"]) + " dependencies not built"

def printReport(artifact, depth = None):
    lists = getList(artifact)
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

def printLookup(artifact):
    gav = getGAV(artifact)
    bestMatch = str(artifact["bestMatchVersion"])
    lists = getList(artifact)
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
    print "tree of GAVs\tBest Match Version\tBlack/White list\t# of not built dependencies\t# of available versions"
    printReport(data,0)

def reportRaw():
    data = readInput()
    checkError(data)
    printReport(data)
    
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
