package org.jboss.da.reports.backend.api;

import java.util.Collections;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jboss.da.reports.api.GAV;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
public class ArtifactTree {
    
    @Getter
    @NonNull
    private GAV gav;
    
    @NonNull
    private Set<ArtifactTree> dependencies;

    public Set<ArtifactTree> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }
    
}
