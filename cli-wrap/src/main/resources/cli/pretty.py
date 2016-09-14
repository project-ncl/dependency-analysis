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

def fancyProductItem(data, element):
    tablen = 4
    tmp = "    " + element + " : " + str(data['leftProduct'][element])
    tmp2 = "    " + element + " : " + str(data['rightProduct'][element])
    tmplen = len(tmp)
    tmp2len = len(tmp2)
    
   
    sys.stdout.write(tmp) 
    
    for num in range (tmplen,40):
        sys.stdout.write(" ")
    sys.stdout.write(tmp2) 
    print

def checkLenLess80(data, element):  
    tmp = "    " + element + " : " + str(data['leftProduct'][element])
    tmp2 = "    " + element + " : " + str(data['leftProduct'][element])
    tmplen = len(tmp)
    tmp2len = len(tmp2)
    return (tmplen + tmp2len) < 80

def printDifference(data):
    tablen = 4

    newline = True

    printInline = checkLenLess80(data,"id") and checkLenLess80(data,"name") and checkLenLess80(data,"version") and checkLenLess80(data,"supportStatus")

    if (printInline):
        sys.stdout.write("Left Product:")
        for num in range (len("Left Product:"),40):
            sys.stdout.write(" ")
        print "Right Product:"
        fancyProductItem(data,"id") 
        fancyProductItem(data,"name")
        fancyProductItem(data,"version")
        fancyProductItem(data,"supportStatus")
        print
    else:
        print("Left Product:")
        print "    id : " + str(data['leftProduct']['id'])
        print "    name : " + str(data['leftProduct']['name'])
        print "    version : " + str(data['leftProduct']['version'])
        print "    supportStatus : " + str(data['leftProduct']['supportStatus'])
        print
        print("Right Product:")
        print "    id : " + str(data['rightProduct']['id'] )
        print "    name : " + str(data['rightProduct']['name'])
        print "    version : " + str(data['rightProduct']['version'])
        print "    supportStatus : " + str(data['rightProduct']['supportStatus'])
        print
        
    if len(data['added']) == 0:
        newline = True
        print "added : NONE"
    else:
        print "added :"
        newline = False
        for module in data['added']:
            print "    groupId : " + module['groupId'] 
            print "    artifactId : " + module['artifactId']
            print "    version : " + module['version']
            print

    if (newline == True):
        print

    if len(data['removed']) == 0:
        newline = True
        print "removed : NONE"
    else:
        newline = False
        print "removed :"
        for module in data['removed']:
            print "    groupId : " + module['groupId'] 
            print "    artifactId : " + module['artifactId']
            print "    version : " + module['version']
            print
    
    if (newline == True):
        print
    
    if len(data['changed']) == 0:
        newline = True
        print "changed : NONE"     
    else:
        newline = False
        print "changed :"
        for module in data['changed']:
            print "    groupId : " + module['groupId'] 
            print "    artifactId : " + module['artifactId']
            tmp = "    " + module['leftVersion'] + "    ->    " + module['rightVersion']
            inLine = len(tmp) < 80
            if (inLine):
                print(tmp)
            else:
                print "    leftVersion : " + module['leftVersion']
                print "    rightVersion : " + module['rightVersion']
            print
        
    if (newline == True):
        print

    if len(data['unchanged']) == 0:
        newline = True
        print "unchanged : NONE"
    else:
        newline = False
        print "unchanged :"
        for module in data['unchanged']:
            print "    groupId : " + module['groupId'] 
            print "    artifactId : " + module['artifactId']
            print "    version : " + module['version']
            print
    if (newline == True):
        print

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
    try:    
        return json.loads(data)
    except ValueError:
        print "Response is not parsable JSON object:"
        print data
        sys.exit(1)   

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

def difference():
    data = readInput()
    checkError(data)
    printDifference(data)

def listCheck():
    data = readInput()
    versions = []
    for gav in data["found"]:
        versions.append(gav["version"])
    print ", ".join(versions)

def hello():
    print "hi"
