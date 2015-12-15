package org.jboss.da.bc.api;

import java.util.Optional;
import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.model.ProjectGeneratorEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface ProjectBuildConfigurationGenerator {

    ProjectGeneratorEntity startBCGeneration(SCMLocator scm, String projectName)
            throws ScmException, PomAnalysisException, CommunicationException;

    ProjectGeneratorEntity iterateBCGeneration(ProjectGeneratorEntity projects)
            throws CommunicationException;

    Optional<Integer> createBC(ProjectGeneratorEntity projects) throws CommunicationException,
            PNCRequestException;
}
