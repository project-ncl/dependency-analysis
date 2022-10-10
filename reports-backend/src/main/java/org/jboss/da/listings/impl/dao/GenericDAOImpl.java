package org.jboss.da.listings.impl.dao;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.da.common.logging.AuditLogger;
import org.jboss.da.listings.api.dao.GenericDAO;
import org.jboss.da.listings.api.model.GenericEntity;

/**
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 */
public abstract class GenericDAOImpl<T extends GenericEntity> implements GenericDAO<T> {

    @PersistenceContext(unitName = "relationdbPU")
    protected EntityManager em;

    protected final Class<T> type;

    public GenericDAOImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public void create(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Provided entity is null.");
        if (entity.getId() != null)
            throw new IllegalArgumentException("Provided entity has non-null ID: " + entity);
        em.persist(entity);
    }

    @Override
    public T read(long id) {
        return em.find(type, id);
    }

    @Override
    public void update(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Provided entity is null.");
        if (entity.getId() == null)
            throw new IllegalArgumentException("Provided entity has null ID: " + entity);
        if (em.find(type, entity.getId()) == null)
            throw new IllegalArgumentException("Provided entity does not exist: " + entity);
        em.merge(entity);
    }

    @Override
    public void delete(T entity) {
        requireNonNull(entity);
        requireNonNull(entity.getId());
        try {
            em.remove(em.getReference(type, entity.getId()));
        } catch (RuntimeException ex) {
            AuditLogger.LOG.info("Deleting entity " + type.getSimpleName() + " with id " + entity.getId() + " failed.");
            throw ex;
        }
        AuditLogger.LOG.info("Deleted entity " + type.getSimpleName() + " with id " + entity.getId() + ".");
    }

    @Override
    public void delete(long id) {
        try {
            T entity = read(id);
            delete(entity);
        } catch (RuntimeException ex) {
            AuditLogger.LOG.info("Deleting entity " + type.getSimpleName() + " with id " + id + " failed.");
            throw ex;
        }
        AuditLogger.LOG.info("Deleted entity " + type.getSimpleName() + " with id " + id + ".");
    }

}
