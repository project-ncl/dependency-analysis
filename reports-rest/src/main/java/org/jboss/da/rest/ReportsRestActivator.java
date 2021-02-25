package org.jboss.da.rest;

import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.da.rest.exceptions.AllExceptionsMapper;
import org.jboss.da.rest.filter.MDCLoggingFilter;

import org.jboss.da.rest.listings.Artifacts;
import org.jboss.da.rest.listings.BlackListImpl;
import org.jboss.da.rest.products.Products;
import org.jboss.da.rest.reports.Reports;
import org.jboss.pnc.pncmetrics.rest.GeneralRestMetricsFilter;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.pncmetrics.rest.TimedMetricFilter;

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
        addMetricsResources(resources);
        addExceptionMappers(resources);
        return resources;
    }

    /**
     * Swagger classes required to generate the API JSON generation
     * 
     * @param resources
     */
    public void addSwaggerResources(Set<Class<?>> resources) {
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        resources.add(SwaggerConfiguration.class);
    }

    /**
     * Add all JAX-RS classes here to get activated!
     * 
     * @param resources
     */
    public void addProjectResources(Set<Class<?>> resources) {
        resources.add(Root.class);
        resources.add(Artifacts.class);
        resources.add(Reports.class);
        resources.add(Products.class);
        resources.add(BlackListImpl.class);
    }

    public void addMetricsResources(Set<Class<?>> resources) {
        resources.add(GeneralRestMetricsFilter.class);
        resources.add(TimedMetric.class);
        resources.add(TimedMetricFilter.class);
        resources.add(MDCLoggingFilter.class);
    }

    private void addExceptionMappers(Set<Class<?>> resources) {
        resources.add(AllExceptionsMapper.class);
    }
}
