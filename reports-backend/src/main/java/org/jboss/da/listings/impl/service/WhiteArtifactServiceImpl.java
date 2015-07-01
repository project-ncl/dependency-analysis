package org.jboss.da.listings.impl.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
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
    private WhiteArtifactDAO artifactDAO;

    @Override
    protected ArtifactDAO<WhiteArtifact> getDAO() {
        return artifactDAO;
    }
}
