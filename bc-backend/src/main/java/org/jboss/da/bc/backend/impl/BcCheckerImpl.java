package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.BcChecker;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * 
 * Provides information about BuildConfigurations in PNC
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class BcCheckerImpl implements BcChecker {

    @Inject
    private PNCConnector pncConnector;

    @Override
    public Optional<BuildConfiguration> lookupBcByScm(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException {
        List<BuildConfiguration> foundBcs = pncConnector
                .getBuildConfigurations(scmUrl, scmRevision);
        if (foundBcs.isEmpty())
            return Optional.empty();
        else
            return Optional.of(foundBcs.get(0));
    }

}
