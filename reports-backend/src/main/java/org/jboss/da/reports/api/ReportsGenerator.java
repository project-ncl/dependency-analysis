package org.jboss.da.reports.api;

import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.model.rest.GAV;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
public interface ReportsGenerator {

    /**
     * Create a report about artifacts given an scm-url
     * @param scml
     * @return Created report
     */
    public Optional<ArtifactReport> getReportFromSCM(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException;

    /**
     * Create an advanced report about artifacts given an scm-url
     * The advanced report will also contain lists of the top-level module dependencies
     * which are:
     * - blacklisted
     * - whitelisted,
     * - community gavs with a best match version
     * - community gavs with built versions
     * - community gavs
     *
     * @param scml
     * @return Created report
     */
    public Optional<AdvancedArtifactReport> getAdvancedReportFromSCM(SCMLocator scml)
            throws ScmException, PomAnalysisException, CommunicationException;

    public Optional<ArtifactReport> getReport(GAV gav, List<Product> products);

    /**
     * Creates a report about built/not built/blacklisted artifacts. It performs searches
     * in the whole available repository. No restrictions on the artifacts belonging to the
     * certain product are applied.
     *
     * @param gav Top-level GAV for which is the report generated
     * @return Created report or empty Optional if the requested GAV was not found in the repository
     * @throws CommunicationException when there is a problem with communication with remote services
     */
    public ArtifactReport getReport(GAV gav) throws CommunicationException,
            FindGAVDependencyException;

    /**
     * Creates an aligment report.
     * @param scml
     * @param productIds Optional list of product ids to filter the result.
     * @return
     */
    public Set<AlignmentReportModule> getAligmentReport(SCMLocator scml,
            boolean useUnknownProducts, Set<Long> productIds) throws ScmException,
            PomAnalysisException;

}
