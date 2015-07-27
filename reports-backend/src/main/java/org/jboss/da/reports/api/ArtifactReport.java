package org.jboss.da.reports.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jboss.da.communication.aprox.model.GAV;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
public class ArtifactReport {
    
    @Getter
    @NonNull
    private GAV gav;
    
    private Set<String> availableVersions = new HashSet<>();
    
    @Getter
    private String bestMatchVersion;
    
    private Set<ArtifactReport> dependencies;
    
    public void setBestMatchVersion(String version){
        availableVersions.add(version);
        bestMatchVersion = version;
    }
    
    public void addAvailableVersion(String version){
        availableVersions.add(version);
    }
    
    public void addDependency(ArtifactReport dependency){
        dependencies.add(dependency);
    }
    
    public Set<String> getAvailableVersions(){
        return Collections.unmodifiableSet(availableVersions);
    }
    
    public Set<ArtifactReport> getDependencies(){
        return Collections.unmodifiableSet(dependencies);
    }
    
    /**
     * Returns true if this artifact and all the dependencies of this artifact have a GAV already in PNC/Brew.
     */
    public boolean isDependencyVersionSatisfied() {
        if (bestMatchVersion == null) {
            return false;
        }
        return dependencies.stream().noneMatch((dependency) -> (!dependency.isDependencyVersionSatisfied()));
    }
    
}
