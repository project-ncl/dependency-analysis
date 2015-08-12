# Dependency Analysis CLI tools

These tools are used for communicating with Dependency Analysis service. There are two executable
scripts, one for developers, the other for admins. Both scripts uses files `listings.sh`, `pretty-lookup.py` and needs to
have these files in the same folder as the executable script.

### For Developers
Download: [listings.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/listings.sh),
[pretty-lookup.py](https://github.com/project-ncl/dependency-analysis/blob/master/cli/pretty-lookup.py),
[da-cli.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/da-cli.sh)

Distribution for developers who need just to check artifacts.

Features are:
* Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black or white list
* List all artifacts in black or white list
* Check all dependencies from pom in working directory and print their Black/White list status
* Check if an artifact was built and if so, see the available built versions of that artifact
* Generate report for an artifact
* Generate reports based on POM with the information about built artifacts and B/W status

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
$ ./da-cli.sh pom --no-transitive
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
[pretty-lookup.py](https://github.com/project-ncl/dependency-analysis/blob/master/cli/pretty-lookup.py),
[da-cli-admin.sh](https://raw.githubusercontent.com/project-ncl/dependency-analysis/master/cli/bwlist-admin.sh)

Distribution for admins who will be also adding and removing artifacts.

Features are the same as for developers plus:
 * Add artifact GROUP_ID:ARTIFACT_ID:VERSION to black or white list
 * Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from black or white list

#### Example
```
$ ./da-cli-admin.sh add black org.jboss.hibernate:hibernate-core:3.4.0
```
```
$ ./da-cli-admin.sh delete black org.jboss.hibernate:hibernate-core:3.4.0
```
