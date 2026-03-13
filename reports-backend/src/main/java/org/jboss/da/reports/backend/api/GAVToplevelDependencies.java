package org.jboss.da.reports.backend.api;

import java.util.Collections;
import java.util.Set;

import org.jboss.da.model.rest.GAV;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
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
