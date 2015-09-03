package org.jboss.da.listings.impl.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.WhiteArtifactService;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@Stateless
public class WhiteArtifactServiceImpl extends ArtifactServiceImpl<WhiteArtifact> implements
        WhiteArtifactService {

    public WhiteArtifactServiceImpl() {
        super(WhiteArtifact.class);
    }

    @Inject
    private BlackArtifactDAO blackArtifactDAO;

    @Inject
    private WhiteArtifactDAO whiteArtifactDAO;

    @Override
    protected ArtifactDAO<WhiteArtifact> getDAO() {
        return whiteArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.STATUS addArtifact(String groupId,
            String artifactId, String version) {
        WhiteArtifact white = new WhiteArtifact(groupId, artifactId, version);
        if (blackArtifactDAO.findArtifact(groupId, artifactId, version) != null) {
            return STATUS.IS_BLACKLISTED;
        }
        if (whiteArtifactDAO.findArtifact(groupId, artifactId, version) != null) {
            return STATUS.NOT_MODIFIED;
        }
        whiteArtifactDAO.create(white);
        return STATUS.ADDED;
    }
}
