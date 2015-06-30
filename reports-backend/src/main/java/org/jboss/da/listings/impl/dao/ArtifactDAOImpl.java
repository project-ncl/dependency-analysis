package org.jboss.da.listings.impl.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public class ArtifactDAOImpl<T extends Artifact> extends GenericDAOImpl<T> implements
        ArtifactDAO<T> {

    public ArtifactDAOImpl(Class clazz) {
        super(clazz);
    }

    public T findArtifactByGAV(String groupId, String artifactId, String version) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(type);
            Root<T> artifact = cq.from(type);
            cq.select(artifact).where(
                    cb.and(cb.equal(artifact.get("artifactId"), artifactId),
                            cb.equal(artifact.get("groupId"), groupId),
                            cb.equal(artifact.get("version"), version)));
            TypedQuery<T> q = em.createQuery(cq);
            return q.getSingleResult();
        } catch (NoResultException e) {
            // ok
        }
        return null;
    }

    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> artifact = cq.from(type);
        cq.select(artifact);
        TypedQuery<T> q = em.createQuery(cq);
        return q.getResultList();
    }

}
