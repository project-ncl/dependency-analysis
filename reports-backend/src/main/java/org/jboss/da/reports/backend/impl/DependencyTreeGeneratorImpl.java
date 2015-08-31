package org.jboss.da.reports.backend.impl;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.NoGAVInRepositoryException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author Dustin Kut Moy Cheung <dcheung@redhat.com>
 *
 */
@ApplicationScoped
public class DependencyTreeGeneratorImpl implements DependencyTreeGenerator {

    @Inject
    private AproxConnector aproxConnector;

    @Override
    public GAVDependencyTree getDependencyTree(SCMLocator scml) {
        try {
            return aproxConnector.getDependencyTreeOfRevision(scml.getScmUrl(), scml.getRevision(),
                    scml.getPomPath());
        } catch (CommunicationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public GAVDependencyTree getDependencyTree(GAV gav) {
        try {
            return aproxConnector.getDependencyTreeOfGAV(gav);
        } catch (CommunicationException | NoGAVInRepositoryException ex) {
            throw new RuntimeException(ex);
        }
    }
}
