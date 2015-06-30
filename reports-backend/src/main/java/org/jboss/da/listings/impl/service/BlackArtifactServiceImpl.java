package org.jboss.da.listings.impl.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
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
    private BlackArtifactDAO artifactDAO;

    @Override
    protected ArtifactDAO<BlackArtifact> getDAO() {
        return artifactDAO;
    }

}
