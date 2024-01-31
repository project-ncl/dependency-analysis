package org.jboss.da.listings.impl.dao;

import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.model.GA;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Stateless
public class GADAOImpl extends GenericDAOImpl<GA> implements GADAO {

    public static final int BATCH_SIZE = 100;

    public GADAOImpl() {
        super(GA.class);
    }

    @Override
    public Optional<GA> findGA(String groupId, String artifactId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<GA> cq = cb.createQuery(type);
            Root<GA> ga = cq.from(type);
            cq.select(ga)
                    .where(cb.and(cb.equal(ga.get("groupId"), groupId), cb.equal(ga.get("artifactId"), artifactId)));
            TypedQuery<GA> q = em.createQuery(cq);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Set<GA> findGAs(Set<org.jboss.da.model.rest.GA> gas) {
        ArrayList<org.jboss.da.model.rest.GA> listOfGAs = new ArrayList<>(gas);
        Set<GA> ret = new HashSet<>();
        for (int i = 0; i <= gas.size(); i += BATCH_SIZE) {
            int to = i + BATCH_SIZE;
            if (to > gas.size()) {
                to = gas.size();
            }
            ret.addAll(findGAs(listOfGAs.subList(i, to)));
        }
        return ret;
    }

    private List<GA> findGAs(List<org.jboss.da.model.rest.GA> gas) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<GA> cq = cb.createQuery(type);
        Root<GA> gaTable = cq.from(type);

        ArrayList<Predicate> ands = new ArrayList<>();
        for (org.jboss.da.model.rest.GA ga : gas) {
            Predicate and = cb.and(
                    cb.equal(gaTable.get("groupId"), ga.getGroupId()),
                    cb.equal(gaTable.get("artifactId"), ga.getArtifactId()));
            ands.add(and);
        }
        cq.select(gaTable).where(cb.or(ands.toArray(Predicate[]::new)));

        TypedQuery<GA> q = em.createQuery(cq);

        return q.getResultList();
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
