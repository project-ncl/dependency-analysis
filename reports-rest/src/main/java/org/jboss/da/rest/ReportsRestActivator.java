package org.jboss.da.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.da.rest.exceptions.AllExceptionsMapper;
import org.jboss.da.rest.filter.MDCLoggingFilter;
import org.jboss.da.rest.listings.BlackListImpl;
import org.jboss.da.rest.reports.Reports;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.da.common.Constants.DA_VERSION;
import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ApplicationPath("/rest/v-" + REST_API_VERSION_REPORTS)
@ApplicationScoped
@OpenAPIDefinition(
        info = @Info(
                title = "Dependency Analyzer",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = DA_VERSION),
        servers = { @Server(url = "/da", description = "Dependency Analyzer") },
        tags = {
                @Tag(name = "lookup", description = "Lookup of artifact versions."),
                @Tag(name = "blocklist", description = "Listings of blocklisted artifacts"),
                @Tag(name = "reports", description = "Get report of dependencies of projects"),
                @Tag(name = "deprecated", description = "Deprecated endpoints.") })
public class ReportsRestActivator extends Application {

    // @Context
    // private ServletConfig servletConfig;

    // @PostConstruct
    // public void init() {
    // configureSwagger();
    // }

    /*
     * private void configureSwagger() { try { var builder = new JaxrsOpenApiContextBuilder().application(this)
     * .resourcePackages(Collections.singleton("org.jboss.da.rest")); if (servletConfig != null) {
     * builder.servletConfig(servletConfig); } builder.buildContext(true); } catch (OpenApiConfigurationException ex) {
     * throw new IllegalArgumentException("Failed to setup OpenAPI configuration", ex); } }
     */

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        // addSwaggerResources(resources);
        addProjectResources(resources);
        addMetricsResources(resources);
        addExceptionMappers(resources);
        addProviders(resources);
        return resources;
    }

    /**
     * Swagger classes required to generate the API JSON generation
     *
     * @param resources public void addSwaggerResources(Set<Class<?>> resources) { resources.add(OpenApiResource.class);
     *        }
     */

    /**
     * Add all JAX-RS classes here to get activated!
     *
     * @param resources
     */
    public void addProjectResources(Set<Class<?>> resources) {
        resources.add(Root.class);
        resources.add(Reports.class);
        resources.add(LookupImpl.class);
        resources.add(BlackListImpl.class);
        resources.add(VersionEndpointImpl.class);
    }

    public void addMetricsResources(Set<Class<?>> resources) {
        resources.add(MDCLoggingFilter.class);
    }

    public void addProviders(Set<Class<?>> resources) {
        resources.add(JacksonProvider.class);
    }

    private void addExceptionMappers(Set<Class<?>> resources) {
        resources.add(AllExceptionsMapper.class);
    }
}
