package org.jboss.da.listings.impl.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.da.common.version.SuffixedVersion;
import org.jboss.da.common.version.VersionParser;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.model.rest.GAV;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ApplicationScoped
public class WhiteArtifactServiceImpl extends ArtifactServiceImpl<WhiteArtifact> implements
        WhiteArtifactService {

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private WhiteArtifactDAO whiteArtifactDAO;

    @Inject
    private GADAO gaDAO;

    @Inject
    private ProductVersionDAO productVersionDAO;

    @Override
    protected ArtifactDAO<WhiteArtifact> getDAO() {
        return whiteArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus addArtifact(
            String groupId, String artifactId, String version, Long productVersionId) {
        Optional<WhiteArtifact> dbArtifact = whiteArtifactDAO.findArtifact(groupId, artifactId,
                version);
        WhiteArtifact artifact = dbArtifact.orElseGet(() -> createArtifact(groupId, artifactId,
                version));

        ProductVersion p = productVersionDAO.read(productVersionId);

        if (p == null) {
            throw new IllegalArgumentException("Wrong productId, product with this id not found");
        }

        if (p.getWhiteArtifacts().contains(artifact)) {
            return ArtifactStatus.NOT_MODIFIED;
        }
        final GA ga = artifact.getGa();

        p.addArtifact(artifact);
        productVersionDAO.update(p);
        return ArtifactStatus.ADDED;
    }

    private WhiteArtifact createArtifact(String groupId, String artifactId, String version) {
        SuffixedVersion parsedVersion = versionParser.parse(version);
        final boolean is3rdParty = !parsedVersion.isSuffixed();
        final String osgiVersion = VersionParser.getOSGiVersion(version);

        GA ga = gaDAO.findOrCreate(groupId, artifactId);
        WhiteArtifact a = new WhiteArtifact(ga, version, currentUser(), osgiVersion, is3rdParty);
        whiteArtifactDAO.create(a);
        return a;
    }

    @Override
    public boolean removeArtifractFromProductVersion(String groupId, String artifactId,
            String version, Long productVersionId) {
        ProductVersion pv = productVersionDAO.read(productVersionId);
        if (pv == null) {
            return false;
        }
        Optional<WhiteArtifact> a = whiteArtifactDAO.findArtifact(groupId, artifactId, version);
        if (a.isPresent()) {
            pv.removeArtifact(a.get());
            productVersionDAO.update(pv);
            return true;
        }
        return false;
    }

    @Override
    public List<WhiteArtifact> getArtifacts(String groupId, String artifactId, String version) {

        // Black listed artifacts can't be queried
        if (blackArtifactService.isArtifactPresent(groupId, artifactId, version)) {
            return new ArrayList<>();
        }
        String osgi = VersionParser.getOSGiVersion(version);

        List<WhiteArtifact> whites = new ArrayList<>();
        Optional<WhiteArtifact> origArtifact = whiteArtifactDAO.findArtifact(groupId, artifactId, version);
        Optional<WhiteArtifact> normArtifact = whiteArtifactDAO.findArtifact(groupId, artifactId, osgi);
        origArtifact.ifPresent(x -> whites.add(x));
        if (!normArtifact.equals(origArtifact)) {
            normArtifact.ifPresent(x -> whites.add(x));
        }
        List<WhiteArtifact> nonDupWhites = new ArrayList<>(new LinkedHashSet<>(whites));
        return nonDupWhites;
    }

    @Override
    public List<WhiteArtifact> getArtifacts(GAV gav) {
        return getArtifacts(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean removeArtifact(String groupId, String artifactId, String version) {
        Optional<WhiteArtifact> artifact = whiteArtifactDAO.findArtifact(groupId, artifactId,
                version);
        if (artifact.isPresent()) {
            List<ProductVersion> productVersions = productVersionDAO
                    .findProductVersionsWithArtifact(groupId, artifactId, version, true);
            for (ProductVersion pv : productVersions) {
                pv.removeArtifact(artifact.get());
                productVersionDAO.update(pv);
            }
            whiteArtifactDAO.delete(artifact.get());
            return true;
        }
        return false;
    }
}
