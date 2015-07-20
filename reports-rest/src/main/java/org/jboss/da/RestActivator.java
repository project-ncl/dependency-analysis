package org.jboss.da;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.da.listings.rest.api.Artifacts;
import org.jboss.da.listings.rest.api.Root;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationPath("/rest")
public class RestActivator extends Application {

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
