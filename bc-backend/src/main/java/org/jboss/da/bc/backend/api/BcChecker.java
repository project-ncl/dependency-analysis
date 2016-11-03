package org.jboss.da.bc.backend.api;

import java.util.List;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;

/**
 * Provides information about BuildConfigurations in PNC
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface BcChecker {

    /**
     * Looks for a BC by SCM URL and SCM revision
     * 
     * @param scmUrl Expected SCM URL in BC
     * @param scmRevision Expected SCM revision in BC
     * @return Optional entity with BuildConfiguration
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    List<BuildConfiguration> lookupBcByScm(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException;

    List<Integer> lookupBcIdsByScm(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException;

}
