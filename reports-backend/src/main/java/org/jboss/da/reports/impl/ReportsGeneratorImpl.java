package org.jboss.da.reports.impl;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.Product;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.api.SCMLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.backend.api.VersionFinder;

/**
 * The implementation of reports, which provides information about
 * built/not built artifacts/blacklisted artifacts
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * @author Honza Brázdil <jbrazdil@redhat.com>
 *
 */
public class ReportsGeneratorImpl implements ReportsGenerator {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private AproxConnector aproxClient;

    @Inject
    private VersionFinder versionFinderImpl;

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private WhiteArtifactService whiteArtifactService;

    @Override
    public ArtifactReport getReport(SCMLocator scml, List<Product> products) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactReport getReport(GAV gav, List<Product> products) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactReport getReport(GAV gav) {
        try {
            if (gav == null)
                throw new IllegalArgumentException("GAV can't be null");
            List<String> versions = versionFinderImpl.getVersionsFor(gav);
            if (versions == null)
                return null;
            ArtifactReport ar = toArtifactReport(gav, versions);
            // TODO Add dependencies to the reports and reports for the dependencies
            // GAVDependencyTree dt = aproxClient.getDependencyTreeOfGAV(gav);
            // addDependencyReports(ar, dt.getDependencyTree());
            return ar;
        } catch (CommunicationException ex) {
            java.util.logging.Logger.getLogger(ReportsGeneratorImpl.class.getName()).log(
                    Level.SEVERE, null, ex);
            return null;
        }
    }

    private ArtifactReport toArtifactReport(GAV gav, List<String> availableVersions) {
        ArtifactReport report = new ArtifactReport(gav);
        report.addAvailableVersions(availableVersions);
        report.setBestMatchVersion(versionFinderImpl.getBestMatchVersionFor(gav, availableVersions));
        report.setBlacklisted(blackArtifactService.isArtifactPresent(gav));
        report.setWhiteListed(whiteArtifactService.isArtifactPresent(gav));
        return report;
    }

    private void addDependencyReports(ArtifactReport ar, Set<GAVDependencyTree> dependencyTree)
            throws CommunicationException {
        for (GAVDependencyTree dt : dependencyTree) {
            List<String> versions = versionFinderImpl.getVersionsFor(dt.getGav());
            if (versions == null) {
                log.warn("Versions for dependency {} was not found", dt.getGav());
                continue;
            }

            ArtifactReport dar = toArtifactReport(dt.getGav(), versions);
            addDependencyReports(dar, dt.getDependencyTree());
            ar.addDependency(dar);
        }
    }

}
