# Dependency Analyzer - User's documentation

## Introduction

This project is a service, which provides information about built artifacts and analyse the projects' dependencies. It can lookup the Red Hat build artifacts and inform the users about alternatives instead of the artifacts used in their projects, produces dependency reports of artifacts and resolves dependency tree.

Dependency Analyzer also maintains a database of blocklisted artifacts, which should help the user to decide, which artifacts to use in their projects.

This project is hosted on [GitHub](https://github.com/project-ncl/dependency-analysis) and developed mainly by the Productization team, but external contributors are welcome.

### Blocklist

The service maintains the database of the blocklisted artifacts. This data in the database can be changed only by authenticated users. In future authorization may be added.

The meaning of the this lists is:

- **Blocklist** — contains artifacts, which should not be used in the projects. There are typically artifacts, which cannot be built, has unacceptable license, contains vulnerabilities, etc. The inserted artifacts can be of community versions of artifacts (e.g. 4.2.18.Final) which will blocklist all redhat builds of that version or of redhat version (e.g. 4.2.18.Final-redhat-1) which will blocklist the specific version.

### Lookup of built artifacts

Dependency Analyzer can lookup the Red Hat built artifacts and tell the user if an artifact was built or not and provide them alternative versions of the artifact. The user gets the information about:

- The latest built version of the artifact
- All built versions of artifacts with the same GroupId, ArtifactId
- Blocklist status of the requested artifact

### Dependency reports

The system is able to provide information about the dependencies of a project. It can analyse a repository specified by the SCM URL (+ revision). This feature extends the lookup and it resolves whole dependency tree and provides information for every artifact in the tree and also some helper data like how many dependencies are not built.

### Data source

Dependency Analyzer is using Indy, an artifact proxy for maven, to get information about built artifacts.

Currently data about built artifacts are gathered from the public product repository ([maven.repository.redhat.com](http://maven.repository.redhat.com/ga)).

The integration with Brew/MEAD repositories is done and Indy will proxy also artifacts from the candidate tags.

Dependency Analyzer also provides built artifacts from the Project Newcastle build system.

## Interfaces

Dependency Analyzer is a server side application. A **REST API** provides the main logic of the system and provides an easy way to use it in other applications.

### REST API

The REST API is based on a Swagger documentation, which provides an easy way to have an up to date documentation and also a simple way to try the REST API from the browser. The path to the Swagger documentation is **`/q/swagger-ui`**. From this endpoint there is also link to the latest API version.
