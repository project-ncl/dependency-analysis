package org.jboss.da.rest;

import org.jboss.da.rest.listings.ArtifactsImpl;

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
public class RestActivator extends Application {

    private static Class<?>[] restClasses = new Class<?>[] { RootImpl.class, ArtifactsImpl.class };

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(restClasses));
    }
}
