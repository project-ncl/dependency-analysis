package org.jboss.da.reports.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.model.api.SCMLocator;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.request.LookupNPMRequest;
import org.jboss.da.reports.model.request.SCMReportRequest;
import org.jboss.da.reports.model.request.VersionsNPMRequest;
import org.jboss.da.reports.model.response.LookupReport;
import org.jboss.da.reports.model.response.NPMLookupReport;
import org.jboss.da.reports.model.response.NPMVersionsReport;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public interface ReportsGenerator {

    /**
     * Create a report about artifacts given an scm-url
     *
     * @param scml
     * @return Created report
     */
    Optional<ArtifactReport> getReportFromSCM(SCMReportRequest scml)
            throws ScmException, PomAnalysisException, CommunicationException;

    /**
     * Create an advanced report about artifacts given an scm-url The advanced report will also contain lists of the
     * top-level module dependencies which are: - blacklisted - whitelisted, - community gavs with a best match version
     * - community gavs with built versions - community gavs
     *
     * @param scml
     * @return Created report
     */
    Optional<AdvancedArtifactReport> getAdvancedReportFromSCM(SCMReportRequest scml)
            throws ScmException, PomAnalysisException, CommunicationException;

    /**
     * Creates an alignment report.
     *
     * @param scml
     * @param productIds Optional list of product ids to filter the result.
     * @return
     */
    Set<AlignmentReportModule> getAlignmentReport(
            SCMLocator scml,
            boolean useUnknownProducts,
            Set<Long> productIds) throws ScmException, PomAnalysisException, CommunicationException;

    Set<BuiltReportModule> getBuiltReport(SCMLocator scml)
            throws ScmException, PomAnalysisException, CommunicationException;

    List<LookupReport> getLookupReportsForGavs(LookupGAVsRequest request) throws CommunicationException;

    List<NPMLookupReport> getLookupReports(LookupNPMRequest request) throws CommunicationException;

    List<NPMVersionsReport> getVersionsReports(VersionsNPMRequest request) throws CommunicationException;

}
