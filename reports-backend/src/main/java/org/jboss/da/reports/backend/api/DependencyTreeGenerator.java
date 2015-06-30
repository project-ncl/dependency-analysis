package org.jboss.da.reports.backend.api;

import org.jboss.da.reports.api.GAV;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
public interface DependencyTreeGenerator {
    
    public ArtifactTree getDependencyTree(SCMLocator scml);
    
    public ArtifactTree getDependencyTree(GAV gav);
    
}
