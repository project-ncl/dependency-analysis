package org.jboss.da.rest;

import org.jboss.da.rest.listings.Artifacts;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationPath("/rest")
public class ReportsRestActivator extends Application {

    private static Class<?>[] restClasses = new Class<?>[] { Root.class, Artifacts.class,
    com.wordnik.swagger.jaxrs.listing.ApiListingResource.class,
    com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class,
    com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class,
    com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class};

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(restClasses));
    }
}
