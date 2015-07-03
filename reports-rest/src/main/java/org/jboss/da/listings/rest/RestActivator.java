package org.jboss.da.listings.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.da.listings.rest.impl.ArtifactsImpl;

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
