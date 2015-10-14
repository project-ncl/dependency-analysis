package org.jboss.da.bc.backend.api;

import org.jboss.da.bc.model.ProjectHiearchy;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface Finalizer {

    public Integer createBCs(String name, String productVersion, ProjectHiearchy toplevelBc,
            String bcSetName) throws Exception;

}
