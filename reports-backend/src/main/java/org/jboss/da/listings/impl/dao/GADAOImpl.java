package org.jboss.da.listings.impl.dao;

import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.model.GA;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

@Stateless
public class GADAOImpl extends GenericDAOImpl<GA> implements GADAO {

    public GADAOImpl() {
        super(GA.class);
    }

    @Override
    public Optional<GA> findGA(String groupId, String artifactId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<GA> cq = cb.createQuery(type);
            Root<GA> ga = cq.from(type);
            cq.select(ga).where(cb.and(cb.equal(ga.get("groupId"), groupId), cb.equal(ga.get("artifactId"), artifactId)));
            TypedQuery<GA> q = em.createQuery(cq);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<GA> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<GA> cq = cb.createQuery(type);
        Root<GA> ga = cq.from(type);
        cq.select(ga);
        TypedQuery<GA> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public GA findOrCreate(String groupId, String artifactId) {
        Optional<GA> oga;
        if ((oga = findGA(groupId, artifactId)).isPresent()) {
            return oga.get();
        }
        create(new GA(groupId, artifactId));
        return findGA(groupId, artifactId).get();
    }

}
