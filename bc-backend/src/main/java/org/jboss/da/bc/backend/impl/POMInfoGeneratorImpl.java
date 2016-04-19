package org.jboss.da.bc.backend.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.backend.api.POMInfo;
import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GAV;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class POMInfoGeneratorImpl implements POMInfoGenerator {

    @Inject
    SCMConnector scm;

    @Inject
    AproxConnector aprox;

    @Override
    public Optional<POMInfo> getPomInfo(String url, String revision, String pomPath) throws ScmException {
        Optional<MavenProject> pom = scm.getPom(url, revision, pomPath);
        return pom.map(x -> toPomInfo(x));
    }

    @Override
    public Optional<POMInfo> getPomInfo(GAV gav) throws CommunicationException, PomAnalysisException {
        Optional<MavenProject> pom = aprox.getPom(gav);
        return pom.map(x -> toPomInfo(x));
    }

    private POMInfo toPomInfo(MavenProject pom) {
        GAV gav = new GAV(pom.getGroupId(), pom.getArtifactId(), pom.getVersion());
        String url = pom.getScm() != null ? pom.getScm().getUrl() : null;
        String tag = pom.getScm() != null ? pom.getScm().getTag() : null;
        return new POMInfo(gav, url, tag, pom.getName());
    }

    @Override
    public Optional<POMInfo> getPomInfo(String scmUrl, String scmRevision, GAV gav) throws ScmException {
        Optional<MavenProject> pom = scm.getPom(scmUrl, scmRevision, gav);
        return pom.map(x -> toPomInfo(x));
    }
}
