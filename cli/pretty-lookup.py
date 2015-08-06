#!/usr/bin/python
import json
import sys
import fileinput

data=""
#for line in sys.stdin:
for line in fileinput.input():
    data += line

jdata = json.loads(data)

for e in jdata:
    lists="no list"
    if e["blacklisted"] and e["whitelisted"]:
        lists="both lists"
    elif e["blacklisted"]:
        lists="black list"
    elif e["whitelisted"]:
        lists="white list"
    print e["groupId"]+":"+e["artifactId"]+":"+e["version"]+"\t"+str(e["bestMatchVersion"])+"\t"+"["+", ".join(e["availableVersions"])+"]\t"+lists
