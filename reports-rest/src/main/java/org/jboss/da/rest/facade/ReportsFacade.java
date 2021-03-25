package org.jboss.da.rest.facade;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.BuiltReportModule;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.model.api.SCMLocator;
import org.jboss.da.reports.model.request.AlignReportRequest;
import org.jboss.da.reports.model.request.BuiltReportRequest;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.request.LookupNPMRequest;
import org.jboss.da.reports.model.request.SCMReportRequest;
import org.jboss.da.reports.model.request.VersionsNPMRequest;
import org.jboss.da.reports.model.response.AdvancedReport;
import org.jboss.da.reports.model.response.AlignReport;
import org.jboss.da.reports.model.response.BuiltReport;
import org.jboss.da.reports.model.response.LookupReport;
import org.jboss.da.reports.model.response.NPMLookupReport;
import org.jboss.da.reports.model.response.NPMVersionsReport;
import org.jboss.da.reports.model.response.Report;
import org.jboss.da.validation.Validation;
import org.jboss.da.validation.ValidationException;

import javax.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class ReportsFacade {

    @Inject
    private ReportsGenerator reportsGenerator;

    @Inject
    private Validation validation;

    public Set<BuiltReport> builtReport(BuiltReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        validation.validation(request, "Getting dependency report for a project specified in a repository URL failed");
        String pomPath = getPomPath(request.getPomPath());
        SCMLocator locator = SCMLocator
                .generic(request.getScmUrl(), request.getRevision(), pomPath, request.getAdditionalRepos());

        Set<BuiltReportModule> builtReport = reportsGenerator.getBuiltReport(locator);
        return Translate.toBuiltReport(builtReport);
    }

    public AlignReport alignReport(AlignReportRequest request)
            throws ScmException, PomAnalysisException, ValidationException, CommunicationException {
        validation.validation(request, "Getting allignment report for project specified in a repository URL failed");
        String pomPath = getPomPath(request.getPomPath());
        SCMLocator locator = SCMLocator
                .generic(request.getScmUrl(), request.getRevision(), pomPath, request.getAdditionalRepos());

        Set<AlignmentReportModule> aligmentReport = reportsGenerator
                .getAligmentReport(locator, request.isSearchUnknownProducts(), request.getProducts());
        return Translate.toAlignReport(aligmentReport);
    }

    public Report scmReport(SCMReportRequest request) throws ScmException, PomAnalysisException, CommunicationException,
            NoSuchElementException, ValidationException {
        validation.validation(request, "Getting dependency report for a project specified in a repository URL failed");
        if (request.getProductVersionIds().size() == 1) { // user inserted ID as empty string
            Iterator<Long> iterator = request.getProductVersionIds().iterator();
            if (iterator.next() == null) {
                iterator.remove();
            }
        }

        if (request.getProductNames().size() == 1) {
            Iterator<String> iterator = request.getProductNames().iterator();
            if ("".equals(iterator.next())) {
                iterator.remove();
            }
        }

        Optional<ArtifactReport> artifactReport = reportsGenerator.getReportFromSCM(request);

        return artifactReport.map(Translate::toReport).orElseThrow(() -> new NoSuchElementException());
    }

    public AdvancedReport advancedScmReport(SCMReportRequest request)
            throws ValidationException, ScmException, PomAnalysisException, CommunicationException {
        validation.validation(request, "Getting dependency report for a project specified in a repository URL failed");
        if (request.getProductVersionIds().size() == 1) { // user inserted ID as empty string
            Iterator<Long> iterator = request.getProductVersionIds().iterator();
            if (iterator.next() == null) {
                iterator.remove();
            }
        }

        if (request.getProductNames().size() == 1) {
            Iterator<String> iterator = request.getProductNames().iterator();
            if ("".equals(iterator.next())) {
                iterator.remove();
            }
        }

        Optional<AdvancedArtifactReport> advancedArtifactReport = reportsGenerator.getAdvancedReportFromSCM(request);

        return advancedArtifactReport.map(Translate::toAdvancedReport).orElseThrow(() -> new NoSuchElementException());
    }

    public List<LookupReport> gavsReport(LookupGAVsRequest gavRequest) throws CommunicationException {
        return reportsGenerator.getLookupReportsForGavs(gavRequest);
    }

    public List<NPMLookupReport> lookupReport(LookupNPMRequest request) throws CommunicationException {
        return reportsGenerator.getLookupReports(request);
    }

    public List<NPMVersionsReport> versionReport(VersionsNPMRequest request) throws CommunicationException {
        return reportsGenerator.getVersionsReports(request);
    }

    private static String getPomPath(String requestPomPath) {
        if (requestPomPath == null || requestPomPath.isEmpty()) {
            return "pom.xml";
        }
        return requestPomPath;
    }
}
