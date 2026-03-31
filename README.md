# Dependency-analysis

This project is a service, which provides information about built artifacts and
analyse the projects' dependencies. It can lookup the Red Hat artifacts and inform
the users about alternatives instead of the artifacts used in their projects, produces
dependency reports of artifacts and resolves dependency tree.

The project is currently based upon Quarkus (it used to use EAP 7).

## Developing

### Build and test the project

The project requires JDK17 or higher and a recent Maven version.

```mvn clean package```

### Developing Documentation

For the documentation (in the `docs` directory), the theme can be previewed locally. Assuming `ruby-devel` is installed and `bundle install` has been run, then run `bundle jekyll serve -l -w -I`


## Deploy the application

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
   * Health  (on http://localhost:8080/q/health/ )

## Configuration

* **Dependency Analyzer (`da.*`)** — defaults are inlined under `da` in `application/src/main/resources/application.yaml` (`da.pnc-url`, `da.indy.*`, `da.lookup-modes`). Override with env vars (e.g. `DA_PNC_URL`, `DA_INDY_INDY_URL`), system properties, or additional entries in `quarkus.config.locations`.

* **Quarkus** — `application/src/main/resources/application.yaml` (HTTP, logging, persistence, etc.).