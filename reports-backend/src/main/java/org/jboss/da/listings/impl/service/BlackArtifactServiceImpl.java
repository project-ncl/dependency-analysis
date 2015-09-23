package org.jboss.da.listings.impl.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationScoped
public class BlackArtifactServiceImpl extends ArtifactServiceImpl<BlackArtifact> implements
        BlackArtifactService {

    @Inject
    private BlackArtifactDAO blackArtifactDAO;

    @Inject
    private WhiteArtifactDAO whiteArtifactDAO;

    @Override
    protected ArtifactDAO<BlackArtifact> getDAO() {
        return blackArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.STATUS addArtifact(String groupId,
            String artifactId, String version) {

        String nonrhVersion = versionParser.removeRedhatSuffix(version);
        String osgiVersion = versionParser.getNonRedhatOSGiVersion(version);

        BlackArtifact artifact = new BlackArtifact(groupId, artifactId, osgiVersion);

        if (blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion) != null) {
            return STATUS.NOT_MODIFIED;
        }

        Set<WhiteArtifact> whites = new HashSet<>();
        whites.addAll(whiteArtifactDAO.findRedhatArtifact(groupId, artifactId, nonrhVersion));
        whites.addAll(whiteArtifactDAO.findRedhatArtifact(groupId, artifactId, osgiVersion));

        STATUS status = STATUS.ADDED;
        for (WhiteArtifact wa : whites) {
            whiteArtifactDAO.delete(wa);
            status = STATUS.WAS_WHITELISTED;
        }
        blackArtifactDAO.create(artifact);
        return status;
    }

    @Override
    public Optional<BlackArtifact> getArtifact(String groupId, String artifactId, String version) {
        String osgiVersion = versionParser.getNonRedhatOSGiVersion(version);

        return Optional.ofNullable(blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion));
    }

    @Override
    public Optional<BlackArtifact> getArtifact(GAV gav) {
        return getArtifact(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return getArtifact(groupId, artifactId, version).isPresent();
    }

}
