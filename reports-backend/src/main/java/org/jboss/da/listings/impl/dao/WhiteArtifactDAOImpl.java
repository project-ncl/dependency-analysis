package org.jboss.da.listings.impl.dao;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.da.common.logging.AuditLogger;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.WhiteArtifact;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@Stateless
public class WhiteArtifactDAOImpl extends ArtifactDAOImpl<WhiteArtifact> implements WhiteArtifactDAO {

    @Override
    public void create(WhiteArtifact entity) {
        try {
            super.create(entity);
        } catch (RuntimeException ex) {
            AuditLogger.LOG.info("Adding allowlisted artifact " + entity.gav() + " failed.");
            throw ex;
        }
        AuditLogger.LOG.info("Added allowlisted artifact " + entity.gav() + ".");
    }

    public WhiteArtifactDAOImpl() {
        super(WhiteArtifact.class);
    }
}
