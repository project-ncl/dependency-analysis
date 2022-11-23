package org.jboss.da.rest.api;

import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.jboss.da.rest.LookupImpl;
import org.jboss.da.rest.api.v1.Artifacts;
import org.jboss.da.rest.api.v1.Products;
import org.jboss.da.rest.api.v1.Reports;
import org.jboss.da.rest.api.v1.Root;
import org.jboss.da.rest.api.v1.SwaggerConfiguration;
import org.jboss.da.rest.exceptions.AllExceptionsMapper;
import org.jboss.da.rest.filter.MDCLoggingFilter;
import org.jboss.da.rest.listings.BlackListImpl;
import org.jboss.pnc.pncmetrics.rest.GeneralRestMetricsFilter;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.pncmetrics.rest.TimedMetricFilter;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ApplicationPath("/rest/v-" + REST_API_VERSION_REPORTS)
public class ApiV1 extends AbstractApi {

    /**
     * Swagger classes required to generate the API JSON generation
     * 
     * @param resources
     */
    public void addSwaggerResources(Set<Class<?>> resources) {
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
        resources.add(LookupImpl.class);
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

    @Override
    public Set<Class<?>> getApiClasses() {
        Set<Class<?>> resources = new HashSet<>();

        addSwaggerResources(resources);
        addProjectResources(resources);
        addMetricsResources(resources);
        addExceptionMappers(resources);

        return resources;
    }
}
