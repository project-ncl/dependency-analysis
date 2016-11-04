package org.jboss.da.listings.impl.dao;

import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.User;

import javax.ejb.Stateless;

/**
 * 
 * @author Honza Brázdil <jbrazdil@redhat.com>
 *
 */
@Stateless
public class UserDAOImpl extends GenericDAOImpl<User> implements UserDAO {

    public UserDAOImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findUser(String keycloakId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(type);
            Root<User> user = cq.from(type);
            cq.select(user).where(cb.equal(user.get("keycloakId"), keycloakId));
            TypedQuery<User> q = em.createQuery(cq);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
