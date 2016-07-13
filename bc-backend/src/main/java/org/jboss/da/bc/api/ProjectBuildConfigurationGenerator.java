package org.jboss.da.bc.api;

import java.util.Optional;
import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.model.backend.ProjectGeneratorEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.model.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface ProjectBuildConfigurationGenerator {

    ProjectGeneratorEntity startBCGeneration(SCMLocator scm, int projectId) throws ScmException,
            PomAnalysisException, CommunicationException;

    ProjectGeneratorEntity iterateBCGeneration(ProjectGeneratorEntity projects)
            throws CommunicationException;

    Optional<Integer> createBC(ProjectGeneratorEntity projects) throws CommunicationException,
            PNCRequestException;
}
