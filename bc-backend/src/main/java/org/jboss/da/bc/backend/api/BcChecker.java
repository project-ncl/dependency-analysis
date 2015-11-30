package org.jboss.da.bc.backend.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;

import java.util.Optional;

/**
 * Provides information about BuildConfigurations in PNC
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
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
    Optional<BuildConfiguration> lookupBcByScm(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException;

}
