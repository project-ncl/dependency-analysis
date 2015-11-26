package org.jboss.da.listings.impl.service;

import java.util.ArrayList;
import java.util.List;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.common.version.VersionParser;

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

    @Override
    protected ArtifactDAO<WhiteArtifact> getDAO() {
        return whiteArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.STATUS addArtifact(String groupId,
            String artifactId, String version) {
        if (!VersionParser.isRedhatVersion(version)) {
            throw new IllegalArgumentException("Version " + version
                    + " doesn't contain redhat suffix");
        }

        WhiteArtifact white = new WhiteArtifact(groupId, artifactId, version);
        if (blackArtifactService.isArtifactPresent(groupId, artifactId, version)) {
            return STATUS.IS_BLACKLISTED;
        }
        if (whiteArtifactDAO.findArtifact(groupId, artifactId, version) != null) {
            return STATUS.NOT_MODIFIED;
        }
        whiteArtifactDAO.create(white);
        return STATUS.ADDED;
    }

    @Override
    public List<WhiteArtifact> getArtifacts(String groupId, String artifactId, String version) {
        String orig = version;
        String osgi = versionParser.getOSGiVersion(version);

        List<WhiteArtifact> whites = new ArrayList<>();
        if (VersionParser.isRedhatVersion(orig)) {
            WhiteArtifact origArtifact = whiteArtifactDAO.findArtifact(groupId, artifactId, orig);
            WhiteArtifact osgiArtifact = whiteArtifactDAO.findArtifact(groupId, artifactId, osgi);
            if (origArtifact != null) {
                whites.add(origArtifact);
            }
            if (osgiArtifact != null && !osgiArtifact.equals(origArtifact)) {
                whites.add(osgiArtifact);
            }
        } else {
            whites.addAll(whiteArtifactDAO.findRedhatArtifact(groupId, artifactId, orig));
            whites.addAll(whiteArtifactDAO.findRedhatArtifact(groupId, artifactId, osgi));
        }
        return whites;
    }

    @Override
    public List<WhiteArtifact> getArtifacts(GAV gav) {
        return getArtifacts(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return !getArtifacts(groupId, artifactId, version).isEmpty();
    }

}
