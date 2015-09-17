package org.jboss.da.communication.pom.qualifier;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Cartographer library already provides an @Produces for CartographerCore,
 * which sadly returns null. We need to add a Qualifier to the @Produces in CartographerProducer
 * to help Weld pick our cartographer producer over the one in the Cartographer library.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface DACartographerCore {
}
