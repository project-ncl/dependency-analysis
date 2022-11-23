package org.jboss.da.rest.api;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;

public abstract class AbstractApi extends Application {
    @Context
    private ServletConfig servletConfig;

    public AbstractApi() {
        try {
            SwaggerConfiguration oasConfig = new SwaggerConfiguration().id(UUID.randomUUID().toString())
                    .prettyPrint(true)
                    .resourceClasses(getApiClasses().stream().map(Class::toString).collect(Collectors.toSet()));
            new JaxrsOpenApiContextBuilder<>().servletConfig(servletConfig)
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public abstract Set<Class<?>> getApiClasses();

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>(getApiClasses());
        // Add Swagger resource
        resources.add(OpenApiResource.class);
        return resources;
    }
}
