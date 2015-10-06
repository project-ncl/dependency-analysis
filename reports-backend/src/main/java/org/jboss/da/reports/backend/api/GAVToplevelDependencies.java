package org.jboss.da.reports.backend.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class GAVToplevelDependencies {

    @Getter
    @NonNull
    private final GAV gav;

    @Getter
    private final Set<GAV> dependencies;

    public GAVToplevelDependencies(GAV gav, Set<GAV> dependencies) {
        this.gav = gav;
        this.dependencies = Collections.unmodifiableSet(dependencies);
    }

}
