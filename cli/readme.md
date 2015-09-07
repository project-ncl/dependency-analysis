# Dependency Analysis CLI tools

These tools are used for communicating with Dependency Analysis service. There are two executable
scripts, one for developers, the other for admins. Both scripts uses files `listings.sh`, `pretty.py` and needs to
have these files in the same folder as the executable script.

### For Developers
Download: [listings.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/listings.sh),
[pretty.py](https://github.com/project-ncl/dependency-analysis/blob/master/cli/pretty.py),
[da-cli.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/da-cli.sh)

Distribution for developers who need just to check artifacts.

Features are:
*      check: Check if an artifact GROUP_ID:ARTIFACT_ID:VERSION is in the black or white list
*       list: List all artifacts in black or white list
*     lookup: Check if an artifact was built and if so, see the available built versions of that artifact
*     report: Generate dependency report for an artifact
*     pom-bw: Check all dependencies from a pom in working directory and print their Black/White list status
* pom-report: Generate reports based on a POM with the information about built artifacts, best match versions
  and B/W status

#### Example
```
$ ./da-cli.sh check black org.jboss.hibernate:hibernate-core:3.4.0
Artifact org.jboss.hibernate:hibernate-core:3.4.0 is NOT blacklisted
```
```
$ ./da-cli.sh list white
test:artifact:1.0
org.jboss.hibernate:hibernate-core:3.4.2
junit:junit:jar:4.11:
```
```
$ ./da-cli.sh pom-bw
None list:  com.fasterxml.jackson.core:jackson-databind:2.4.4
None list:  javax.enterprise:cdi-api:1.2
White list: javax.ws.rs:javax.ws.rs-api:2.0
White list: junit:junit:4.11
None list:  org.hibernate:hibernate-core:4.2.14.SP4-redhat-1
None list:  org.jboss.arquillian.junit:arquillian-junit-container:1.1.8.Final
None list:  org.jboss.arquillian.protocol:arquillian-protocol-servlet:1.1.8.Final
Black list: org.jboss.as:jboss-as-arquillian-container-managed:7.2.0.Final
```
### For Admins
Download: [listigs.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/listings.sh),
[pretty.py](https://github.com/project-ncl/dependency-analysis/blob/master/cli/pretty.py),
[da-cli-admin.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/bwlist-admin.sh)

Distribution for admins who will be also adding and removing artifacts.

Features are the same as for developers plus:
*    add: Add artifact GROUP_ID:ARTIFACT_ID:VERSION to the black or white list
* delete: Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from the black or white list

#### Example
```
$ ./da-cli-admin.sh add black org.jboss.hibernate:hibernate-core:3.4.0
```
```
$ ./da-cli-admin.sh delete black org.jboss.hibernate:hibernate-core:3.4.0
```
