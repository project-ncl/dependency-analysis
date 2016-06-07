# Dependency-analysis

## Build a project
  - mvn clean package

### Build a project and run integration tests in testsuite module
  - mvn clean verify -DtestsuiteContainer=/path/to/EAP/dir
  - You can exclude tests, which use remote services, using -PexcludeRemoteTests

### Deploy/work with local jboss instance

  - make sure your instance of EAP is running (/path/to/EAP/bin/standalone.sh)

  - DEPLOY: 
    mvn clean package wildfly:deploy

    - deploys 'application' module (application/target/dependency-analysis.ear)
    - deploys to standalone/data/content instead of standalone/deployments
    - default endpoints 
        http://localhost:8080/da/ 
        http://localhost:8080/da-bcg/
    - swagger provides a generated UI to test the endpoints 
        http://localhost:8080/da/doc/doc
        http://localhost:8080/da-bcg/doc

  - UNDEPLOY:
    mvn wildfly:undeploy

### Working with JBoss Developer Studio (JBDS)

  - Deploying through IDE via 'Run on Server' (e.g. JBDS) seems to corrupt expected endpoints, 
    use maven command to deploy/undeploy

  - Project DA uses lombok.jar to generate boilerplate getters/setters/constructors via annotations,
    download/install into your IDE as per https://projectlombok.org/download.html

  - Workspace errors in .js, .xml, other files can be ignored as long as Maven build completes
    successfully from command line

### Run/debug integration tests in 'testsuite' through IDE (JBDS/Eclipse)

  - Install Arquillian support:
    - Help -> Install New Software -> Work With -> http://download.jboss.org/jbosstools/updates/stable/luna/
    - Select 'Arquillian support' under 'JBoss Web and Java EE Development'
    - Create a run configuration for any test class/test method you want to run (right click - Run As - JUnit)
    - Open 'Arquillian' tab under the run configuration to set properties (jbossHome, managementPort)

  - Run/Debug your run configuration
