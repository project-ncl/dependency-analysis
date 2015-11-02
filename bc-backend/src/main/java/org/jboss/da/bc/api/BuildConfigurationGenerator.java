package org.jboss.da.bc.api;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;

import java.util.Optional;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface BuildConfigurationGenerator {

    GeneratorEntity startBCGeneration(SCMLocator scm, String productName, String productVersion)
            throws ScmException, PomAnalysisException, CommunicationException;

    GeneratorEntity iterateBCGeneration(GeneratorEntity projects) throws CommunicationException;

    Optional<Integer> createBC(GeneratorEntity projects) throws CommunicationException,
            PNCRequestException;
}
