package org.jboss.da.reports.backend.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GAV;
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
    SCMConnector scmConnector;

    @Override
    public GAVDependencyTree getDependencyTree(SCMLocator scml) throws ScmException,
            PomAnalysisException {
        return scmConnector.getDependencyTreeOfRevision(scml.getScmUrl(), scml.getRevision(),
                scml.getPomPath(), scml.getRepositories());
    }

    @Override
    public GAVDependencyTree getDependencyTree(GAV gav) throws CommunicationException,
            FindGAVDependencyException {
        return aproxConnector.getDependencyTreeOfGAV(gav);
    }

    @Override
    public GAVDependencyTree getDependencyTree(String url, String revision, GAV gav)
            throws ScmException, PomAnalysisException {
        return scmConnector.getDependencyTreeOfRevision(url, revision, gav);
    }

    @Override
    public GAVToplevelDependencies getToplevelDependencies(SCMLocator scml) throws ScmException,
            PomAnalysisException {
        GAVDependencyTree tree = getDependencyTree(scml);
        return treeToToplevel(tree);
    }

    @Override
    public GAVToplevelDependencies getToplevelDependencies(GAV gav) throws CommunicationException,
            FindGAVDependencyException {
        return treeToToplevel(getDependencyTree(gav));
    }

    @Override
    public GAVToplevelDependencies getToplevelDependencies(String url, String revision, GAV gav)
            throws ScmException, PomAnalysisException {
        Set<GAV> deps = scmConnector.getToplevelDependencyOfRevision(url, revision, gav);
        return new GAVToplevelDependencies(gav, deps);
    }

    private GAVToplevelDependencies treeToToplevel(GAVDependencyTree tree){
        Set<GAV> dependencies = tree.getDependencies().stream()
                .map(x -> x.getGav())
                .collect(Collectors.toSet());

        return new GAVToplevelDependencies(tree.getGav(), dependencies);
    }
}
