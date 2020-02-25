package org.jboss.da.listings.impl.dao;

import javax.ejb.Stateless;

import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.GA;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import java.util.List;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Stateless
public class BlackArtifactDAOImpl extends ArtifactDAOImpl<BlackArtifact> implements BlackArtifactDAO {

    public BlackArtifactDAOImpl() {
        super(BlackArtifact.class);
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
