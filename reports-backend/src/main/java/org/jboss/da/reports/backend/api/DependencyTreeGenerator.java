package org.jboss.da.reports.backend.api;

import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface DependencyTreeGenerator {

    public GAVDependencyTree getDependencyTree(SCMLocator scml) throws ScmException,
            PomAnalysisException;

    public GAVDependencyTree getDependencyTree(GAV gav) throws CommunicationException,
            FindGAVDependencyException;

    public GAVDependencyTree getDependencyTree(String url, String revision, GAV gav)
            throws ScmException, PomAnalysisException;

    public GAVToplevelDependencies getToplevelDependencies(SCMLocator scml) throws ScmException,
            PomAnalysisException;

    public GAVToplevelDependencies getToplevelDependencies(GAV gav) throws CommunicationException,
            FindGAVDependencyException;

    public GAVToplevelDependencies getToplevelDependencies(String url, String revision, GAV gav)
            throws ScmException, PomAnalysisException;

}
