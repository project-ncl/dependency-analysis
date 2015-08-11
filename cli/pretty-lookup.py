#!/usr/bin/python
import json
import sys
import fileinput


def read_json_object(e):
    lists="no list"
    if "message" in e:
        # message key present when there is an error
        print(e["message"])
        sys.exit(1)
    if e["blacklisted"] and e["whitelisted"]:
        lists="both lists"
    elif e["blacklisted"]:
        lists="black list"
    elif e["whitelisted"]:
        lists="white list"

    if "availableVersions" in e:
        prettyAvailableVersions = "\t[" + ", ".join(e["availableVersions"]) + "]"
    else:
        prettyAvailableVersions = ""

    print(e["groupId"]+":"+e["artifactId"]+":"+e["version"]+"\t"+str(e["bestMatchVersion"]) + prettyAvailableVersions + "\t"+lists)


data=""
for line in fileinput.input():
    data += line

jdata = json.loads(data)

if type(jdata) is dict:
    read_json_object(jdata)

elif type(jdata) is list:
    for e in jdata:
        read_json_object(e)
else:
    print("Couldn't parse the JSON data: " + data)
