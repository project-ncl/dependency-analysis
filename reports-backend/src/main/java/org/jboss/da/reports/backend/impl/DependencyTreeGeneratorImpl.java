package org.jboss.da.reports.backend.impl;

import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
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
        return aproxConnector.getDependencyTreeOfRevision(scml.getScmUrl(),
                scml.getRevision(), scml.getPomPath());
    }

    @Override
    public GAVDependencyTree getDependencyTree(GAV gav) {
        return aproxConnector.getDependencyTreeOfGAV(gav);
    }
}
