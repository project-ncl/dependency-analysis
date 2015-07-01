package org.jboss.da.listings.impl.dao;

import static java.util.Objects.requireNonNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.da.listings.api.dao.GenericDAO;
import org.jboss.da.listings.api.model.GenericEntity;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public abstract class GenericDAOImpl<T extends GenericEntity> implements GenericDAO<T> {

    @PersistenceContext(unitName = "relationdbPU")
    protected EntityManager em;

    protected final Class<T> type;

    public GenericDAOImpl(Class<T> type) {
        this.type = type;
    }

    public void create(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Provided entity is null.");
        if (entity.getId() != null)
            throw new IllegalArgumentException("Provided entity has non-null ID: " + entity);
        em.persist(entity);
    }

    public T read(long id) {
        return em.find(type, id);
    }

    public void update(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Provided entity is null.");
        if (entity.getId() == null)
            throw new IllegalArgumentException("Provided entity has null ID: " + entity);
        if (em.find(type, entity.getId()) == null)
            throw new IllegalArgumentException("Provided entity does not exist: " + entity);
        em.merge(entity);
    }

    public void delete(T entity) {
        requireNonNull(entity);
        requireNonNull(entity.getId());
        em.remove(entity);
    }

    public void delete(long id) {
        T entity = read(id);
        delete(entity);
    }

}
