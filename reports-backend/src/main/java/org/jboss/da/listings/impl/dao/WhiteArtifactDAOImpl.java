package org.jboss.da.listings.impl.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.WhiteArtifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@Stateless
public class WhiteArtifactDAOImpl extends ArtifactDAOImpl<WhiteArtifact> implements
        WhiteArtifactDAO {

    public WhiteArtifactDAOImpl() {
        super(WhiteArtifact.class);
    }

    @Override
    public List<WhiteArtifact> findRedhatArtifact(String groupId, String artifactId, String version) {
        String dotVersion = version + ".redhat-%";
        String dashVersion = version + "-redhat-%";

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<WhiteArtifact> cq = cb.createQuery(type);
        Root<WhiteArtifact> artifact = cq.from(type);
        cq.select(artifact).where(
                cb.and(cb.equal(artifact.get("artifactId"), artifactId),
                        cb.equal(artifact.get("groupId"), groupId),
                        cb.or(cb.like(artifact.get("version"), dotVersion),
                                cb.like(artifact.get("version"), dashVersion))));
        TypedQuery<WhiteArtifact> q = em.createQuery(cq);
        List<WhiteArtifact> list = q.getResultList();
        return list;
    }
}
