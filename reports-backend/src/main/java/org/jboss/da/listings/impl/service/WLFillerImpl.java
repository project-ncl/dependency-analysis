package org.jboss.da.listings.impl.service;

import org.apache.maven.scm.ScmException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.WLFiller;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WLFillerImpl implements WLFiller {

    @Inject
    private Logger log;

    @Inject
    private PomAnalyzer analyzer;

    @Inject
    private WhiteArtifactServiceImpl whiteService;

    @Inject
    private ProductVersionServiceImpl productVersionService;

    @Inject
    private AproxConnector aprox;

    @Inject
    private SCM scmManager;

    @Override
    public WLStatus fillWhitelistFromPom(String scmUrl, String revision, String pomPath,
            List<String> repositories, long productId) {
        if (!checkProduct(productId)) {
            return WLStatus.PRODUCT_NOT_FOUND;
        }
        try {
            fillWLFromPom(getPomListFromGit(scmUrl, revision, pomPath, repositories), productId);
        } catch (ScmException | GalleyMavenException | PomAnalysisException e) {
            log.error(e.getMessage());
            return WLStatus.ANALYSER_ERROR;
        }
        return WLStatus.FILLED;
    }

    @Override
    public WLStatus fillWhitelistFromGAV(String groupId, String artifactId, String version,
            long productId) {
        if (!checkProduct(productId)) {
            return WLStatus.PRODUCT_NOT_FOUND;
        }
        try {
            Optional<InputStream> is = aprox.getPomStream(new GAV(groupId, artifactId, version));
            if (is.isPresent()) {
                MavenPomView view = analyzer.getMavenPomView(is.get());
                fillWLFromPom(view, productId);
            } else {
                return WLStatus.ANALYSER_ERROR;
            }
        } catch (CommunicationException | ConfigurationParseException | GalleyMavenException e) {
            log.error(e.getMessage());
            return WLStatus.ANALYSER_ERROR;
        }
        return WLStatus.FILLED;
    }

    private MavenPomView getPomListFromGit(String scmUrl, String revision, String pomPath,
            List<String> repositories) throws GalleyMavenException, PomAnalysisException,
            ScmException {

        if (repositories == null) {
            repositories = new ArrayList<>();
        }

        File clonedDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        MavenPomView pom = analyzer.getGitPomView(clonedDir, pomPath, repositories);

        return pom;
    }

    private void fillWLFromPom(MavenPomView v, long productId) throws GalleyMavenException {
        List<DependencyView> dependencies = v.getAllManagedDependencies();
        for (DependencyView d : dependencies) {
            GA ga = new GA(d.getGroupId(), d.getArtifactId());
            Artifact a = new Artifact(ga, d.getVersion());
            whiteService.addArtifact(a.getGa().getGroupId(), a.getGa().getArtifactId(),
                    a.getVersion(), productId);
        }
    }

    private boolean checkProduct(long productId) {
        Optional<ProductVersion> pv = productVersionService.getProductVersion(productId);
        return pv.isPresent();
    }
}
