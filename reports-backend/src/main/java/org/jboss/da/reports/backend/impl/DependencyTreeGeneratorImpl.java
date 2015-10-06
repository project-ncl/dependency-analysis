package org.jboss.da.reports.backend.impl;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.reports.backend.api.GAVToplevelDependencies;

/**
 *
 * @author Dustin Kut Moy Cheung <dcheung@redhat.com>
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ApplicationScoped
public class DependencyTreeGeneratorImpl implements DependencyTreeGenerator {

    @Inject
    private AproxConnector aproxConnector;

    @Inject
    SCMConnector SCMConnector;

    @Override
    public GAVDependencyTree getDependencyTree(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException {
        return SCMConnector.getDependencyTreeOfRevision(scml.getScmUrl(), scml.getRevision(),
                scml.getPomPath());
    }

    @Override
    public Optional<GAVDependencyTree> getDependencyTree(GAV gav) throws CommunicationException {
        return aproxConnector.getDependencyTreeOfGAV(gav);
    }

    @Override
    public GAVToplevelDependencies getToplevelDependencies(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException {
        GAVDependencyTree tree = getDependencyTree(scml);

        return treeToToplevel(tree);
    }

    @Override
    public Optional<GAVToplevelDependencies> getToplevelDependencies(GAV gav) throws CommunicationException {
        return getDependencyTree(gav).map(tree -> treeToToplevel(tree));
    }

    private GAVToplevelDependencies treeToToplevel(GAVDependencyTree tree){
        Set<GAV> dependencies = tree.getDependencies().stream()
                .map(x -> x.getGav())
                .collect(Collectors.toSet());

        return new GAVToplevelDependencies(tree.getGav(), dependencies);
    }
}
