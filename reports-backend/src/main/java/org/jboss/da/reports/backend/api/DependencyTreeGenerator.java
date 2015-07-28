package org.jboss.da.reports.backend.api;

import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
public interface DependencyTreeGenerator {
    
    public GAVDependencyTree getDependencyTree(SCMLocator scml);
    
    public GAVDependencyTree getDependencyTree(GAV gav);
    
}
