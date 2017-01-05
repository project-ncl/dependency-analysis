#!/usr/bin/env python3
import inspect
import re
import subprocess
import requests
import argparse
import sys
import json
import da_cli_script
import pretty
import testsuite
import asyncio
import websockets

def addBlack(gav):
    da_cli_script.add_artifacts(gav, "black")
def addWhite(gav, product):
    da_cli_script.add_artifacts(gav, "white", product)

def addWhitelistProd(product, status):
    if matchProd(product) and matchStatus(status):
        da_cli_script.add_artifacts(None, "whitelist-product", product, status)
    else:
        exit()

def deleteArtifactProduct(color, gav=None, product = None):
    da_cli_script.delete_artifacts(color, gav, product)


def check(color_par,gav):
    if (color_par == "b") or color_par == "black":
        pass
    else:
        print("Unrecognized color. Supported color is b[lack]")
        exit()
    if matchGAV(gav):
        groupId, artifactId, version = gav.split(":",3)
    else:
        exit()
    response = da_cli_script.requests_get(da_cli_script.da_server + "/listings/blacklist/gav?groupid="+groupId+"&artifactid="+artifactId+"&version="+version)
    output = response.json()
    output = (json.dumps(output))

    if '"contains": true' in output:
        print("Artifact "+gav +" is blacklisted - actual verisions in list: ")
        print(pretty.listCheck(json.loads(output)))
    elif '"contains": false' in output:
        print("Artifact "+gav +" is NOT blacklisted")
    else:
        print("Error checking " +gav)

def setColor(color_par):
    if color_par == "white" or color_par == "w":
        color = "white"
    elif color_par == "black" or color_par == "b":
        color = "black"
    else: 
        exit()
    return color

def matchGAV(gav):
    pattern = re.compile("^[^:]+:[^:]+:[^:]+$")
    if not pattern.match(gav):
        print("the GAV is not in format GROUP_ID:ARTIFACT_ID:VERSION : " + gav)
        return False
    return True

def matchGA(ga):
    pattern = re.compile("^[^:]+:[^:]+$")
    if not pattern.match(ga):
        print("the GAV is not in format GROUP_ID:ARTIFACT_ID : " + ga)
        return False
    return True
    
def matchProd(product):
    pattern = re.compile("^[^:]+:[^:]+$")
    if not pattern.match(product):
        print("the product is not in format PRODUCT_NAME:VERSION STATUS : " + product)
        return False
    return True
    
def matchStatuses(statuses):
    if statuses == None:
        return True
    for status in statuses.split(","):
        if not status in "SUPPORTED,SUPERSEDED,UNSUPPORTED,UNKNOWN".split(","):
            print("Status can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN")
            return False
    return True

def matchStatus(status):
    if not status in "SUPPORTED,SUPERSEDED,UNSUPPORTED,UNKNOWN".split(","):
        print("Status can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN")
        return False
    return True
        
def updateFun(product, status):
    if matchProd(product) and matchStatus(status):
        da_cli_script.update_artifacts(product, status)
    else:
        exit()
        
def matchGAPCVS(gavs):
    pattern = re.compile("^([a-zA-Z_][a-zA-Z0-9\._-]*)(:[a-zA-Z_][a-zA-Z0-9\._-]*)(:[a-zA-Z_][a-zA-Z-]*(:[a-zA-Z_][a-zA-Z-]*)??)??(:[a-zA-Z0-9\._-]+)(:compile|:provided|:runtime|:test|:system)?$")
    if not pattern.match(gavs):
        return False
    
    return True

def pom_bw_junit_xml(argv, parser):
    transitive = False
    path = ""
    product = ""
    if len(argv) < 3:
        print("Missing path")
        exit()
    if argv[2] == "--transitive":
        transitive = True
        if len(argv) < 4:
            print("Missing path")
            exit()
        path = argv[3]
        try:
            product = argv[4]
            if len(argv) > 5:
                print("Bad arguments! For help use: ./da-cli.py --help")
        except IndexError:
            if len(argv) > 4:
                print("Bad arguments! For help use: ./da-cli.py --help")
    else:
        path = argv[2]
        try:
            product = argv[3]
            if len(argv) > 4:
                print("Bad arguments! For help use: ./da-cli.py --help")
        except IndexError:
            if len(argv) > 5:
                print("Bad arguments! For help use: ./da-cli.py --help")
  
    command = "cd "+path +" && mvn dependency:list -DappendOutput=true"
    if transitive:
        command += " -DexcludeTransitive=true"
    try:
        output = subprocess.check_output(command, shell=True)
    except (subprocess.CalledProcessError):
        print("Path does not exist!")
        exit()
    packages = set()
    for line in output.splitlines():
        line = line.replace("[INFO]    ","")
        if (line.find("[INFO]") == -1):
            if matchGAPCVS(line):
                gav = line.split(":",5)[0] +":" + line.split(":",5)[1] + ":" + line.split(":",5)[3]
                packages.add(gav)
            else:
                exit()
    testsuite.main(packages, "junit.xml", product)


def formatGAVjsonRequest(groupId, artifactId, version):
    return "\"groupId\":\""+ groupId + "\", \"artifactId\":\""+ artifactId +"\", \"version\":\""+ version  +"\""

def report():
    style = "pretty"
    parser = argparse.ArgumentParser()
    parser.add_argument("report")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--json", action="store_true")
    group.add_argument("--raw", action="store_true")
    parser.add_argument("--products")
    parser.add_argument("--productIDs")
    parser.add_argument("gav")
    args = parser.parse_args()
    if (not matchGAV(args.gav)):
        exit()
    groupId, artifactId, version = args.gav.split(":",3)
    if args.raw:
        style = "raw"
    if args.json:
        style = "json"

    products = ""
    if (args.products != None):
        products = args.products
        products = products.replace(",","\",\"")
        products = "\""+products+"\""
        products = "[" + products + "]"
    else:
        products = "[]"
        
    query = "{\"jsonrpc\": \"2.0\",  \"method\":\"reports.gav\", \"id\": \"request_0\" ,  \"params\":"   
    query += "{"
    query += formatGAVjsonRequest(groupId, artifactId, version)
        
    query += ",\"productNames\":" + products 

    if (args.productIDs != None):
        productIDs = "[" + args.productIDs + "]"
    else:
        productIDs = "[]"
    query += ",\"productVersionIds\":" + productIDs 
 
    query += "}"
    query += "}"
    
    output = asyncio.get_event_loop().run_until_complete(get_response(query))

    if style == "pretty":
        if "errorType" in output:
            print(output['errorMessage'])
            exit()
        pretty_out = pretty.report(output)
        print(pretty_out)
    elif style == "raw":
        print(pretty.reportRaw(output))
    else:
        print(json.dumps(output))
        
        
def parseGAV(gav):
    pattern = re.compile("^(?P<groupID>[a-zA-Z_][a-zA-Z0-9\._-]*)(?P<artifactID>:[a-zA-Z_][a-zA-Z0-9\._-]*)(:[a-zA-Z_][a-zA-Z-]*(:[a-zA-Z_][a-zA-Z-]*)??)??(?P<version>:[a-zA-Z0-9\._-]+)(:compile|:provided|:runtime|:test|:system)?$") 
    result = pattern.match(gav)

    if result != None:
        return(result.group('groupID')+result.group('artifactID')+result.group('version'))
    else:
        print("the GAV is not in format GROUP_ID:ARTIFACT_ID:VERSION: " + gav)
        return None

def lookup():
    parser = argparse.ArgumentParser()
    parser.add_argument("lookup")

    parser.add_argument("--products")
    parser.add_argument("--productIDs")
    parser.add_argument("gav", nargs='?')
    args = parser.parse_args()
    
    gavs = []
    rawGavs = []
    if (args.gav != None):
        gav = parseGAV(args.gav)
        if gav != None and matchGAV(gav):
            gavs.append(gav)
        #groupId, artifactId, version = args.gav.split(":",3)
    else:
        rawGavs = sys.stdin.readlines()
    for gav in rawGavs:
        gav = gav[:-1]
        gav = parseGAV(gav)
        if (gav != None and matchGAV(gav)):
            gavs.append(gav)
        else:
            exit()   
    products = ""
    if (args.products != None):
        products = args.products
        products = products.replace(",","\",\"")
        products = "\""+products+"\""
        products = "[" + products + "]"
    else:
        products = "[]"
        
    query = "{\"jsonrpc\": \"2.0\",  \"method\":\"reports.lookup.gav\", \"id\": \"request_0\" ,  \"params\":"   
    query += "{"
    query += "\"productNames\":" + products 
    
    if (args.productIDs != None):
        productIDs = "[" + args.productIDs + "]"
    else:
        productIDs = "[]"
    query += ",\"productVersionIds\":" + productIDs 
    
    query += ", \"gavs\": ["
    inserted = False
    for gav in gavs:
        groupId, artifactId, version = gav.split(":",3)
        query += "{" + formatGAVjsonRequest(groupId, artifactId, version) + "},"
        inserted = True
    if inserted:
        query = query[:-1]
 
    query += "]}"
    query += "}"
    
    output = asyncio.get_event_loop().run_until_complete(get_response(query))
    
    if "errorType" in output:
            print(output['errorMessage'])
            exit()
    pretty_out = pretty.lookup(output)
    print(pretty_out)

def scm_report():
    style = "pretty"
    parser = argparse.ArgumentParser()
    parser.add_argument("scm-report")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--json", action="store_true")
    group.add_argument("--raw", action="store_true")
    parser.add_argument("--products")
    parser.add_argument("--productIDs")
    parser.add_argument("--repository")
    parser.add_argument("scm")
    parser.add_argument("tag")
    parser.add_argument("pom_path",metavar="pom-path")
    args = parser.parse_args()

    if args.raw:
        style = "raw"
    if args.json:
        style = "json"

    products = ""
    repositories = ""
    if (args.products != None):
        products = args.products
        products = products.replace(",","\",\"")
        products = "\""+products+"\""
        products = "[" + products + "]"
    else:
        products = "[]"
        
    if (args.repository != None):
        repositories = args.repository
        repositories = repositories.replace(",","\",\"")
        repositories = "\""+repositories+"\""
        repositories = "[" + repositories + "]"
    else:
        repositories = "[]"
        
    
    query = "{\"jsonrpc\": \"2.0\",  \"method\":\"reports.scm\", \"id\": \"request_0\" ,  \"params\":"   
    query += "{"
    query += "\"productNames\":" + products 

    if (args.productIDs != None):
        productIDs = "[" + args.productIDs + "]"
    else:
        productIDs = "[]"
    query += ",\"productVersionIds\":" + productIDs 
    
    query += ", \"scml\" : {" + "\"scmUrl\" :\"" +args.scm + "\",\"revision\":\"" + args.tag + "\",\"pomPath\": \"" + args.pom_path + "\",\"repositories\": " + repositories + "}" 
    query += "}"
    query += "}"
    
    output = asyncio.get_event_loop().run_until_complete(get_response(query))

    if style == "pretty":
        if "errorType" in output:
            print(output['errorMessage'])
            exit()
        pretty_out = pretty.report(output)
        print(pretty_out)
    elif style == "raw":
        print(pretty.reportRaw(output))
    else:
        print(json.dumps(output))
    
def scm_report_adv():
    style = "pretty"
    parser = argparse.ArgumentParser()
    parser.add_argument("scm-report-advanced")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--json", action="store_true")
    group.add_argument("--raw", action="store_true")
    parser.add_argument("--products")
    parser.add_argument("--productIDs")
    parser.add_argument("--repository")
    parser.add_argument("scm")
    parser.add_argument("tag")
    parser.add_argument("pom_path",metavar="pom-path")
    args = parser.parse_args()

    if args.raw:
        style = "raw"
    if args.json:
        style = "json"

    products = ""
    repositories = ""
    if (args.products != None):
        products = args.products
        products = products.replace(",","\",\"")
        products = "\""+products+"\""
        products = "[" + products + "]"
    else:
        products = "[]"
        
    if (args.repository != None):
        repositories = args.repository
        repositories = repositories.replace(",","\",\"")
        repositories = "\""+repositories+"\""
        repositories = "[" + repositories + "]"
    else:
        repositories = "[]"
        
    query = "{\"jsonrpc\": \"2.0\",  \"method\":\"reports.scmAdvanced\", \"id\": \"request_0\" ,  \"params\":"    
    query += "{"   
    query += "\"productNames\":" + products

    if (args.productIDs != None):
        productIDs = "[" + args.productIDs + "]"
    else:
        productIDs = "[]"
    query += ",\"productVersionIds\":" + productIDs 
    
    query += ", \"scml\" : {" + "\"scmUrl\" :\"" +args.scm + "\",\"revision\":\"" + args.tag + "\",\"pomPath\": \"" + args.pom_path + "\",\"repositories\": " + repositories + "}" 
    query += "}"
    query += "}"
    
    output = asyncio.get_event_loop().run_until_complete(get_response(query))
    if style == "pretty":
        if "errorType" in output:
            print(output['errorMessage'])
            exit()
        pretty_out = pretty.reportAdvSum(output)
        pretty_out += pretty.reportAdv(output)
        print(pretty_out)
    elif style == "raw":
        print(pretty.reportAdv(output))
    else:
        print(json.dumps(output))
        
def align_report():
    
    style = "pretty"
    parser = argparse.ArgumentParser()
    parser.add_argument("align-report")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--unknown", action="store_true")
    #parser.add_argument("--products") #maybe future extension
    parser.add_argument("--productIDs")
    parser.add_argument("--repository")
    parser.add_argument("scm")
    parser.add_argument("tag")
    parser.add_argument("pom_path",metavar="pom-path",nargs='?')
    args = parser.parse_args()

    if args.json:
        style = "json"
    
    pom_path = ""
    if args.pom_path != None:
        pom_path = args.pom_path

    searchUnknownProducts = "false"
    if args.unknown:
        searchUnknownProducts = "true"
    #products = ""
    repositories = ""
    '''if (args.products != None):
        products = args.products
        products = products.replace(",","\",\"")
        products = "\""+products+"\""
        products = "[" + products + "]"
    else:
        products = "[]"
    '''    
    if (args.repository != None):
        repositories = args.repository
        repositories = repositories.replace(",","\",\"")
        repositories = "\""+repositories+"\""
        repositories = "[" + repositories + "]"
    else:
        repositories = "[]"        
    
    query = "{\"jsonrpc\": \"2.0\",  \"method\": \"reports.align\", \"id\": \"request_0\" ,  \"params\":"        
    query += "{"
    #query += "\"productNames\":" + products 
    if (args.productIDs != None):
        productIDs = "[" + args.productIDs + "]"
    else:
        productIDs = "[]"
    query += "\"products\":" + productIDs 
    
    query += ", " + "\"scmUrl\" :\"" +args.scm + "\",\"revision\":\"" + args.tag + "\",\"pomPath\": \"" + pom_path + "\",\"additionalRepos\": " + repositories + ",\"searchUnknownProducts\": \"" +searchUnknownProducts  +"\""
    query += "}"
    query += "}"
    

    output = asyncio.get_event_loop().run_until_complete(get_response(query))
    if style == "pretty":
        if "errorType" in output:
            print(output['errorMessage'])
            exit()
        pretty_out = pretty.reportAlign(output)
        subprocess.call("echo \"" + pretty_out + "\" | column -t -s '\t' ", shell=True)
    else:
        print(json.dumps(output))

def prod_difference():
    import operator
    style = "pretty"
    parser = argparse.ArgumentParser()
    parser.add_argument("difference")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("leftID")
    parser.add_argument("rightID")
    args = parser.parse_args()
    leftID = 0
    rightID = 0
    
    if args.json:
        style = "json"
        
    try:
        leftID = int(args.leftID)
        rightID = int(args.rightID)
    except ValueError:
        print("That's not a number!")
        exit()
    
    output = ""
    output = da_cli_script.requests_get(da_cli_script.da_server + "/products/diff?leftProduct="+str(leftID)+"&rightProduct="+str(rightID)).json()
    
    if style == "pretty":
        if "errorType" in output:
            print(output['errorMessage'])
            exit()
        else:
            pretty_out = pretty.difference(output)
            print(pretty_out)
            exit()
    else:
        print(json.dumps(output))    
              
async def get_response(query):
    try:
        async with websockets.connect("ws://"+da_cli_script.da_server_ws) as websocket:
            await websocket.send(query)
            output = await websocket.recv()
        try:
            output = json.loads(output)["result"]
            return output
        except KeyError:
            print(json.loads(output)["error"]["message"])
            if "data" in json.loads(output)["error"]:
                print(json.loads(output)["error"]["data"])
            exit()
    except websockets.exceptions.InvalidHandshake:
        print("Server " + "ws://"+da_cli_script.da_server_ws + " not found!")
        exit()
