# Dependency-analysis

## Dependency analyzer CLI
  - You can found the CLI in **cli-wrap/** folder
  - Documantation for CLI can be found here: http://project-ncl.github.io/dependency-analysis/users-documentation.html

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
    
### Setup application
  - Setup Postgre database
  - Datasource configuration and connection information in the standalone.xml
      datasource has to be named "PostgresDA"
  - JDBC for PostgreSQL is needed. You have to add it to standalone.xml
  - Add to the folder ```<EAP_HOME>/modules/system/layers/base/org/postgresql/main/``` a file named 
    module.xml and a file with the driver 
    (download from [here](https://jdbc.postgresql.org/download/postgresql-9.3-1103.jdbc4.jar))
  - The Dependency Analysis project uses a JSON configuration file for its configuration 
    and is found in ```common/src/main/resources/da-config.json``` or it can be set using property da-config-file
  - You can set values to cofigure Dependency Analyser
    * keycloak% - keycloak settings
    * pncServer - link to pnc server
    * aproxServer - link to Indy server
    * aproxGroup - Indy group
    
### Run/debug integration tests in 'testsuite' through IDE (JBDS/Eclipse)

  - Install Arquillian support:
    - Help -> Install New Software -> Work With -> http://download.jboss.org/jbosstools/updates/stable/luna/
    - Select 'Arquillian support' under 'JBoss Web and Java EE Development'
    - Create a run configuration for any test class/test method you want to run (right click - Run As - JUnit)
    - Open 'Arquillian' tab under the run configuration to set properties (jbossHome, managementPort)

  - Run/Debug your run configuration
