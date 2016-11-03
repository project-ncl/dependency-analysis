package org.jboss.da.rest;

import org.jboss.da.rest.listings.Artifacts;
import org.jboss.da.rest.reports.Reports;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;
import org.jboss.da.rest.products.Products;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ApplicationPath("/rest/v-" + REST_API_VERSION_REPORTS)
public class ReportsRestActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        addSwaggerResources(resources);
        addProjectResources(resources);
        return resources;
    }

    /**
     * Swagger classes required to generate the API JSON generation
     * @param resources
     */
    public void addSwaggerResources(Set<Class<?>> resources) {
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        resources.add(SwaggerConfiguration.class);
    }

    /**
     * Add all JAX-RS classes here to get activated!
     * @param resources
     */
    public void addProjectResources(Set<Class<?>> resources) {
        resources.add(Root.class);
        resources.add(Artifacts.class);
        resources.add(Reports.class);
        resources.add(Products.class);
    }
}
