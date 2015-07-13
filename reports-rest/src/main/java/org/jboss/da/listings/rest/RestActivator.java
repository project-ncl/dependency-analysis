package org.jboss.da.listings.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import org.jboss.da.listings.rest.impl.ArtifactsImpl;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationPath("/rest")
public class RestActivator extends Application {

    private static Class<?>[] restClasses = new Class<?>[] { RootImpl.class, ArtifactsImpl.class,
            com.wordnik.swagger.jaxrs.listing.ApiListingResource.class,
            com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class,
            com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class,
            com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class };

    public RestActivator() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath("http://localhost:8080/api");
        beanConfig.setResourcePackage("io.swagger.resources");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(restClasses));
    }
}
