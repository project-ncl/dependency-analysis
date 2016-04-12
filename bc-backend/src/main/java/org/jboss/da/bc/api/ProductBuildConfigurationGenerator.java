package org.jboss.da.bc.api;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.model.backend.ProductGeneratorEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;

import java.util.Optional;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface ProductBuildConfigurationGenerator {

    ProductGeneratorEntity startBCGeneration(SCMLocator scm, int productId, String productVersion)
            throws ScmException, PomAnalysisException, CommunicationException;

    ProductGeneratorEntity iterateBCGeneration(ProductGeneratorEntity projects)
            throws CommunicationException;

    Optional<Integer> createBC(ProductGeneratorEntity projects) throws CommunicationException,
            PNCRequestException;
}
