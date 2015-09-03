package org.jboss.da.listings.impl.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

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
@Stateless
public class BlackArtifactServiceImpl extends ArtifactServiceImpl<BlackArtifact> implements
        BlackArtifactService {

    public BlackArtifactServiceImpl() {
        super(BlackArtifact.class);
    }

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
        BlackArtifact artifact = new BlackArtifact(groupId, artifactId, version);
        WhiteArtifact white;
        if ((white = whiteArtifactDAO.findArtifact(groupId, artifactId, version)) != null) {
            whiteArtifactDAO.delete(white);
            blackArtifactDAO.create(artifact);
            return STATUS.WAS_WHITELISTED;
        }
        if (blackArtifactDAO.findArtifact(groupId, artifactId, version) != null) {
            return STATUS.NOT_MODIFIED;
        }
        blackArtifactDAO.create(artifact);
        return STATUS.ADDED;
    }

}
