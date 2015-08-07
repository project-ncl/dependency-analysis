package org.jboss.da.rest.reports;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.rest.reports.model.LookupReport;
import org.jboss.da.rest.reports.model.Report;
import org.jboss.da.rest.reports.model.SCMRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * Main end point for the reports
 * 
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Path("/reports")
@Api(value = "/reports", description = "Get report of dependencies of projects")
public class Reports {

    @Inject
    private VersionFinder versionFinder;

    @Inject
    private ReportsGenerator reportsGenerator;

    @Inject
    private WhiteArtifactService whiteArtifactService;

    @Inject
    private BlackArtifactService blackArtifactService;

    @POST
    @Path("/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            hidden = true)
    // TODO unhide when the method will be implemented
    public Report scmGenerator(SCMRequest scmRequest) {
        throw new NotImplementedException();
    }

    @POST
    @Path("/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get dependency report for a GAV " // TODO change when dependencies will be implemented
                    + "(Currently the dependencies and dependency_versions_satisfied don't contains usefull values)")
    public Report gavGenerator(GAV gavRequest) {
        return toReport(reportsGenerator.getReport(gavRequest));
    }

    @POST
    @Path("/lookup/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup built versions for the list of provided GAVs")
    public List<LookupReport> lookupGav(List<GAV> gavRequest) throws CommunicationException {
        List<LookupReport> reportsList = new ArrayList<>();
        for (GAV gav : gavRequest) {
            reportsList.add(toLookupReport(gav));
        }
        return reportsList;
    }

    private Report toReport(ArtifactReport report) {
        return new Report(report.getGav().getGroupId(), report.getGav().getArtifactId(), report
                .getGav().getVersion(), new ArrayList<String>(report.getAvailableVersions()),
        // TODO change when dependencies will be implemented
                report.getBestMatchVersion(), false, new ArrayList<Report>(),
                report.isBlacklisted(), report.isWhiteListed());
    }

    private LookupReport toLookupReport(GAV gav) throws CommunicationException {
        return new LookupReport(gav, versionFinder.getBestMatchVersionFor(gav), isBlacklisted(gav),
                isWhitelisted(gav));
    }

    private boolean isBlacklisted(GAV gav) {
        return blackArtifactService.isArtifactPresent(gav.getGroupId(), gav.getArtifactId(),
                gav.getVersion());
    }

    private boolean isWhitelisted(GAV gav) {
        return whiteArtifactService.isArtifactPresent(gav.getGroupId(), gav.getArtifactId(),
                gav.getVersion());
    }

}
