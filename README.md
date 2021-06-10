# Dependency-analysis

## Dependency analyzer CLI
  - You can found the CLI in **cli-wrap/** folder
  - Documentation for CLI can be found here: http://project-ncl.github.io/dependency-analysis/users-documentation.html

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
  - Setup PostgreSQL database
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
    * indyServer - link to Indy server
    * indyGroup - Indy group

### Run/debug integration tests in 'testsuite' through IDE (JBDS/Eclipse)

  - Install Arquillian support:
    - Help -> Install New Software -> Work With -> http://download.jboss.org/jbosstools/updates/stable/luna/
    - Select 'Arquillian support' under 'JBoss Web and Java EE Development'
    - Create a run configuration for any test class/test method you want to run (right click - Run As - JUnit)
    - Open 'Arquillian' tab under the run configuration to set properties (jbossHome, managementPort)

  - Run/Debug your run configuration


### Metrics support

PNC tracks metrics of JVM and its internals via Dropwizard Metrics. The metrics can currently be reported to a Graphite server by specifying as system property or environment variables those properties:
- metrics\_graphite\_server (mandatory)
- metrics\_graphite\_port (mandatory)
- metrics\_graphite\_prefix (mandatory)
- metrics\_graphite\_interval (optional)

If the `metrics_graphite_interval` variable (interval specified in seconds) is not specified, we'll use the default value of 60 seconds to report data to Graphite.

The graphite reporter is configured to report rates per second and durations in terms of milliseconds.
