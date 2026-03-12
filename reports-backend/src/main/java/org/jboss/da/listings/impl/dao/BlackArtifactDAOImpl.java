package org.jboss.da.listings.impl.dao;

import org.jboss.da.common.logging.AuditLogger;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.GA;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.util.List;

/**
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BlackArtifactDAOImpl extends ArtifactDAOImpl<BlackArtifact> implements BlackArtifactDAO {

    public BlackArtifactDAOImpl() {
        super(BlackArtifact.class);
    }

    @Override
    public void create(BlackArtifact entity) {
        try {
            super.create(entity);
        } catch (RuntimeException ex) {
            AuditLogger.LOG.info("Add blocklisted artifact " + entity.gav() + " .failed");
            throw ex;
        }
        AuditLogger.LOG.info("Added blocklisted artifact " + entity.gav() + ".");
    }

    @Override
    public List<BlackArtifact> findArtifacts(String groupId, String artifactId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BlackArtifact> cq = cb.createQuery(type);
        Root<BlackArtifact> artifact = cq.from(type);
        Join<BlackArtifact, GA> ga = artifact.join("ga");
        cq.select(artifact)
                .where(cb.and(cb.equal(ga.get("artifactId"), artifactId), cb.equal(ga.get("groupId"), groupId)));
        TypedQuery<BlackArtifact> q = em.createQuery(cq);
        return q.getResultList();
    }
}
