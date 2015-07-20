# Black & White list CLI tools

These tools are used for communicating with Black & White lists of artifacts. There are two executable
scripts, one for developers, the other for admins. Both scripts uses file `listings.sh` and needs to
have the file in the same folder as the executable script.

### For Developers
Download: [listigs.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/listings.sh), [bwlist.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/bwlist.sh)

Distribution for developers who need just to check artifacts.

Features are:
* Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black or white list
* List all artifacts in black or white list
* Check all dependencies from pom in working directory and print their Black/White list status

#### Example
```
$ ./bwlist.sh check black org.jboss.hibernate:hibernate-core:3.4.0
Artifact org.jboss.hibernate:hibernate-core:3.4.0 is NOT blacklisted
```
```
$ ./bwlist.sh list white
test:artifact:1.0
org.jboss.hibernate:hibernate-core:3.4.2
junit:junit:jar:4.11:
```
```
$ ./bwlist.sh pom --no-transitive
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
Download: [listigs.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/listings.sh), [bwlist-admin.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/bwlist-admin.sh)

Distribution for admins who will be also adding and removing artifacts.

Features are the same as for developers plus:
 * Add artifact GROUP_ID:ARTIFACT_ID:VERSION to black or white list
 * Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from black or white list

#### Example
```
$ ./bwlist-admin.sh add black org.jboss.hibernate:hibernate-core:3.4.0
```
```
$ ./bwlist-admin.sh delete black org.jboss.hibernate:hibernate-core:3.4.0
```
