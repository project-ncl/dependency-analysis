package org.jboss.da.listings.impl.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.model.rest.GAV;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
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
        boolean is3rdParty = false;
        if (!VersionParser.isRedhatVersion(version)) {
            is3rdParty = true;
        }

        GA ga = gaDAO.findOrCreate(groupId, artifactId);

        WhiteArtifact white = new WhiteArtifact(ga, version, versionParser.getOSGiVersion(version),
                is3rdParty);

        if (blackArtifactService.isArtifactPresent(groupId, artifactId, version)) {
            return ArtifactStatus.IS_BLACKLISTED;
        }
        Optional<WhiteArtifact> dbWhite = whiteArtifactDAO.findArtifact(groupId, artifactId,
                version);
        ProductVersion p = productVersionDAO.read(productVersionId);
        if (p == null) {
            throw new IllegalArgumentException("Wrong productId, product with this id not found");
        }
        if (dbWhite.isPresent()) {
            if (p.getWhiteArtifacts().contains(dbWhite.get())) {
                return ArtifactStatus.NOT_MODIFIED;
            } else {
                p.addArtifact(dbWhite.get());
                productVersionDAO.update(p);
                return ArtifactStatus.ADDED;
            }

        }
        whiteArtifactDAO.create(white);
        p.addArtifact(white);
        productVersionDAO.update(p);
        return ArtifactStatus.ADDED;
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
        String osgi = versionParser.getOSGiVersion(version);

        List<WhiteArtifact> whites = new ArrayList<>();
        if (VersionParser.isRedhatVersion(version)) {
            Optional<WhiteArtifact> origArtifact = whiteArtifactDAO
                    .findArtifact(groupId, artifactId, version);
            Optional<WhiteArtifact> osgiArtifact = whiteArtifactDAO.findArtifact(groupId,
                    artifactId, osgi);
            if (origArtifact.isPresent()) {
                whites.add(origArtifact.get());
            }
            if (osgiArtifact.isPresent() && !osgiArtifact.equals(origArtifact)) {
                whites.add(osgiArtifact.get());
            }
        } else {
            Optional<WhiteArtifact> rhA = whiteArtifactDAO.findArtifact(groupId, artifactId,
                    osgi);
            rhA.ifPresent(x -> whites.add(rhA.get()));
            Optional<WhiteArtifact> a = whiteArtifactDAO.findArtifact(groupId, artifactId, version);
            a.ifPresent(x -> whites.add(a.get()));
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
                    .findProductVersionsWithArtifact(groupId, artifactId, version);
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
