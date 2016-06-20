package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.BcChecker;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<BuildConfiguration> lookupBcByScm(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException {
        List<BuildConfiguration> foundBcs = pncConnector
                .getBuildConfigurations(scmUrl, scmRevision);
        return foundBcs;
    }

    @Override
    public List<Integer> lookupBcIdsByScm(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException {
        List<BuildConfiguration> bcs = lookupBcByScm(scmUrl, scmRevision);
        List<Integer> bcIds = bcs.stream()
                .map(x -> x.getId())
                .collect(Collectors.toList());
        
        return bcIds;
    }
}
