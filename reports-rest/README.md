# reports-rest

The `reports-rest` module is used to provide REST endpoints to:

- obtain listings of white and black artifacts
- get dependency information of a project or GAV.

## Documentation
The REST endpoints are documented using [Swagger](http://swagger.io/)

## Adding a new JAX-RS class
Swagger imposes some restrictions on automatic scanning of JAX-RS classes. As
such, for now we'll have to specify the JAX-RS classes we want to activate in
the `ReportsRestActivator` class, method `addProjectResources`.
