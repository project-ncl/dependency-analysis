package org.jboss.da.listings.impl.dao;

import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Optional;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 *
 */
@ApplicationScoped
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
