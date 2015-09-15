package org.jboss.da.listings.impl.service;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

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

        String osgiVersion = osgiParser.getOSGiVersion(removeRedhatSuffix(version));

        BlackArtifact artifact = new BlackArtifact(groupId, artifactId, osgiVersion);

        if (blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion) != null) {
            return STATUS.NOT_MODIFIED;
        }

        List<WhiteArtifact> whites = whiteArtifactDAO.findRedhatArtifact(groupId, artifactId,
                osgiVersion);
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
        String osgiVersion = osgiParser.getOSGiVersion(removeRedhatSuffix(version));

        return Optional.ofNullable(blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion));
    }

    @Override
    public Optional<BlackArtifact> getArtifact(GAV gav) {
        return getArtifact(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    private String removeRedhatSuffix(String version) {
        return redhatSuffixPattern.matcher(version).replaceFirst("");
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return getArtifact(groupId, artifactId, version).isPresent();
    }

}
