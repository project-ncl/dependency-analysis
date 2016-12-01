#!/usr/bin/env python3
import json
import sys
import fileinput


def formatProduct(product, supportStatus=True):
    if supportStatus:
        return product["name"] + ":" + product["version"] + ":" + product["supportStatus"]
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
    output = ""
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

    output += indent + gav + "\t" + bestMatch + "\t" + lists + "\t" + depsSatisfied +  "\t" + versions + "\n"

    for dep in artifact["dependencies"]:
        if depth is None:
            output += printReport(dep)
        else:
            output += printReport(dep, depth + 1)
    return output

def printReportAdvSum(data):
    output = ""
    output += "Blacklisted artifacts:\t" + getGAVList(data["blacklistedArtifacts"]) + "\n"

    output += "Whitelisted artifacts:" + "\n"
    for whitelist in data["whitelistedArtifacts"]:
        output += "  " + getGAV(whitelist) + "\t" + ", ".join(map(formatProduct, whitelist["products"])) + "\n"

    output += "Built community artifacts:" + "\n"
    for bestMatch in data["communityGavsWithBestMatchVersions"]:
        output += "  " + getGAV(bestMatch) + "\t" + bestMatch["bestMatchVersion"] + "\n"

    output += "Community artifacts with other built version:" + "\n"
    for builtVersion in data["communityGavsWithBuiltVersions"]:
        output += "  " + getGAV(builtVersion) + "\t" +  ", ".join(builtVersion["availableVersions"]) + "\n"

    output += "Community artifacts:\t" + getGAVList(data["communityGavs"]) + "\n"
    return output

def printReportAlign(data):
    output = ""
    output += "Internaly built:" + "\n"
    for module in data['internallyBuilt']:
        output += "  " + module['groupId'] + ":" + module['artifactId'] + "\n"
        for dependency in module['gavProducts']:
            output += "    " + getGAV(dependency) + " - " + ", ".join(map(formatVersionProduct,dependency['gavProducts'])) + "\n"
    output += "\n"

    output += "Built in different version:" + "\n"
    for module in data['builtInDifferentVersion']:
        output += "  " + module['groupId'] + ":" + module['artifactId'] + "\n"
        for dependency in module['gavProducts']:
            output += "    " + getGAV(dependency) + " - " + ", ".join(map(formatVersionProduct,dependency['gavProducts'])) + "\n"
    output += "\n"

    output += "Not built:"
    for module in data['notBuilt']:
        output += "  " + module['groupId'] + ":" + module['artifactId'] + "\n"
        for dependency in module['gavs']:
            output += "    " + getGAV(dependency) + "\n"
    output += "\n"

    output += "Blacklisted:" + "\n"
    for module in data['blacklisted']:
        output += "  " + module['groupId'] + ":" + module['artifactId'] + "\n"
        for dependency in module['gavs']:
            output += "    " + getGAV(dependency) + "\n"
            
    return output

def fancyProductItem(data, element):
    output = ""
    tablen = 4
    tmp = "    " + element + " : " + str(data['leftProduct'][element])
    tmp2 = "    " + element + " : " + str(data['rightProduct'][element])
    tmplen = len(tmp)
    tmp2len = len(tmp2)
    
   
    output += tmp 
    
    for num in range (tmplen,40):
        output += " "
    output += tmp2
    output += "\n"
    return output

def checkLenLess80(data, element):  
    tmp = "    " + element + " : " + str(data['leftProduct'][element])
    tmp2 = "    " + element + " : " + str(data['leftProduct'][element])
    tmplen = len(tmp)
    tmp2len = len(tmp2)
    return (tmplen + tmp2len) < 80

def printDifference(data):
    tablen = 4
    output = ""

    newline = True


    printInline = checkLenLess80(data,"id") and checkLenLess80(data,"name") and checkLenLess80(data,"version") and checkLenLess80(data,"supportStatus")

    if (printInline):
        output +="Left Product:\t\t\t\t"
        #for num in range (len("Left Product:"),40):
            #sys.stdout.write(" ")
        output += "Right Product:\n"
        output += fancyProductItem(data,"id") 
        output += fancyProductItem(data,"name")
        output += fancyProductItem(data,"version")
        output += fancyProductItem(data,"supportStatus")
        output += "\n"
    else:
        output += "Left Product:\n"
        output += "    id : " + str(data['leftProduct']['id']+ "\n")
        output += "    name : " + str(data['leftProduct']['name']+ "\n")
        output += "    version : " + str(data['leftProduct']['version']+ "\n")
        output += "    supportStatus : " + str(data['leftProduct']['supportStatus']+ "\n")
        output += "\n"
        output +=("Right Product:")
        output += "    id : " + str(data['rightProduct']['id'] + "\n")
        output += "    name : " + str(data['rightProduct']['name']+ "\n")
        output += "    version : " + str(data['rightProduct']['version']+ "\n")
        output += "    supportStatus : " + str(data['rightProduct']['supportStatus'] + "\n")
        output += "\n"
        
    if len(data['added']) == 0:
        newline = True
        output += "added : NONE \n"
    else:
        output += "added :"
        newline = False
        for module in data['added']:
            output += "    groupId : " + module['groupId'] + "\n"
            output += "    artifactId : " + module['artifactId'] + "\n"
            output += "    version : " + module['version'] + "\n"
            output += "\n"

    if (newline == True):
        output += "\n"

    if len(data['removed']) == 0:
        newline = True
        output += "removed : NONE\n"
    else:
        newline = False
        output += "removed :\n"
        for module in data['removed']:
            output += "    groupId : " + module['groupId'] + "\n"
            output += "    artifactId : " + module['artifactId'] + "\n"
            output += "    version : " + module['version'] + "\n"
            output += "\n"
    
    if (newline == True):
        output += "\n"
    
    if len(data['changed']) == 0:
        newline = True
        output += "changed : NONE\n"     
    else:
        newline = False
        output += "changed :\n"
        for module in data['changed']:
            output += "    groupId : " + module['groupId'] + "\n"
            output += "    artifactId : " + module['artifactId'] + "\n"
            tmp = "    " + module['leftVersion'] + "    ->    " + module['rightVersion'] + "\n"
            inLine = len(tmp) < 80
            if (inLine):
                output +=(tmp)
            else:
                output += "    leftVersion : " + module['leftVersion'] + "\n"
                output += "    rightVersion : " + module['rightVersion'] + "\n"
            output += "\n"
        
    if (newline == True):
        output += "\n"

    if len(data['unchanged']) == 0:
        newline = True
        output += "unchanged : NONE\n"
    else:
        newline = False
        output += "unchanged :"
        for module in data['unchanged']:
            output += "    groupId : " + module['groupId'] + "\n"
            output += "    artifactId : " + module['artifactId'] + "\n"
            output += "    version : " + module['version'] + "\n"
            output += "\n"
    if (newline == True):
        output += "\n"
    return output

def printLookup(artifact):
    gav = getGAV(artifact)
    bestMatch = str(artifact["bestMatchVersion"])
    lists = getList(artifact, True)
    versions = getAvailableVersions(artifact, True)

    return gav + "\t" + bestMatch + "\t" + lists + "\t" + versions + "\n"

def readInput(data):
    try:    
        return json.loads(data)
    except ValueError:
        print("Response is not parsable JSON object:")
        print(data)
        sys.exit(1)   

def checkError(data):
    if "message" in data:
        # message key present when there is an error
        print(data["message"])
        sys.exit(1)

def report(data):
    checkError(data)
    print("tree of GAVs\tExact Match Version\tBlack/White list\t# of not built dependencies\t# of available versions")
    return printReport(data,0) 

def reportRaw(data):
    checkError(data)
    return printReport(data) 

def reportAdv(data):
    checkError(data)
    print("tree of GAVs\tExact Match Version\tBlack/White list\t# of not built dependencies\t# of available versions")
    return printReport(data["report"], 0) 
    
def reportAdvSum(data):
    checkError(data)
    return printReportAdvSum(data)

def reportAlign(data):
    checkError(data)
    return printReportAlign(data)
    
def lookup(data):
    checkError(data)
    output = ""
    for artifact in data:
        output += printLookup(artifact)
    return output
    
def difference(data):
    checkError(data)
    return printDifference(data)

def listCheck(data):
    versions = []
    for gav in data['found']:
        versions.append(gav['version'])
    return ", ".join(versions)

def hello():
    print("hi")
    
