package org.jboss.da.listings.impl.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.GA;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
public class ArtifactDAOImpl<T extends Artifact> extends GenericDAOImpl<T> implements ArtifactDAO<T> {

    public ArtifactDAOImpl(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public Optional<T> findArtifact(String groupId, String artifactId, String version) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(type);
            Root<T> artifact = cq.from(type);
            Join<T, GA> ga = artifact.join("ga");
            cq.select(artifact)
                    .where(
                            cb.and(
                                    cb.equal(ga.get("artifactId"), artifactId),
                                    cb.equal(ga.get("groupId"), groupId),
                                    cb.equal(artifact.get("version"), version)));
            TypedQuery<T> q = em.createQuery(cq);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> findArtifact(Set<GA> gas) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> artifact = cq.from(type);
        cq.select(artifact).where(artifact.get("ga").in(gas));
        TypedQuery<T> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> artifact = cq.from(type);
        cq.select(artifact);
        TypedQuery<T> q = em.createQuery(cq);
        return q.getResultList();
    }

}
