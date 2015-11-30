package org.jboss.da.bc.backend.api;

import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface Finalizer {

    public Integer createBCs(String name, String productVersion, ProjectHiearchy toplevelBc,
            String bcSetName) throws CommunicationException, PNCRequestException;

}
