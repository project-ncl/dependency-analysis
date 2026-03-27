# Dependency-analysis

This project is a service, which provides information about built artifacts and
analyse the projects' dependencies. It can lookup the Red Hat artifacts and inform
the users about alternatives instead of the artifacts used in their projects, produces
dependency reports of artifacts and resolves dependency tree.

The project is currently based upon Quarkus (it used to use EAP 7).

## Build and test the project

```mvn clean package```

### Deploy the application

The application requires a running PostgreSQL instance. While this isn't configured by default
adding e.g.
```
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/postgres
quarkus.datasource.username=postgres
```
to an `application/src/resources/application.properties` and then building the application module would suffice.

It may then be started with

```
java -jar target/dependency-analysis-runner.jar
```

It listens on http://localhost:8080/ and the default endpoint is:
   * http://localhost:8080/rest/v-1

The Quarkus application has been configured with:
   * Swagger (on http://localhost:8080/q/swagger-ui/ )

### Configuration
The Dependency Analysis project uses a JSON configuration file for its configuration and is found in `common/src/main/resources/da-config.json` or it can be set using property da-config-file e.g.

```
java -Dda-config-file=da-config-file.json -jar target/dependency-analysis-runner.jar
```
