package org.jboss.da.bc.backend.api;

import java.util.Optional;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface POMInfoGenerator {

    public Optional<POMInfo> getPomInfo(String url, String revision, String pomPath)
            throws ScmException;

    public Optional<POMInfo> getPomInfo(GAV gav) throws CommunicationException,
            PomAnalysisException;

    public Optional<POMInfo> getPomInfo(String scmUrl, String scmRevision, GAV gav)
            throws ScmException;

}
