package org.jboss.da.listings.api.dao;

import org.jboss.da.listings.api.model.GenericEntity;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public interface GenericDAO<T extends GenericEntity> {

    /**
     * Create new entity in database. The provided entity MUST NOT be null, MUST have null ID and MUST NOT already exist in the
     * database. Data MUST conform to the database constraints. When successful, the entity ID is set properly.
     * 
     * @param entity Entity to be created.
     */
    void create(T entity);

    /**
     * Retrieve entity from database by given ID.
     * 
     * @param id ID of the requested entity.
     * @return The found entity instance or null if the entity does not exist.
     */
    T read(long id);

    /**
     * Updates entity in database. The provided entity MUST NOT be null, MUST have non-null ID and MUST already exist in
     * database. Data MUST conform to the database constraints.
     * 
     * @param entity New state of the entity to be updated.
     */
    void update(T entity);

    /**
     * Deletes given entity from the database. This entity MUST be present in the database. Deletion MUST NOT cause
     * (referential) constraint violation.
     * 
     * @param entity Entity to be deleted.
     */
    void delete(T entity);

    /**
     * Deletes entity with the given ID from the database. This entity MUST be present in the database. Deletion MUST NOT cause
     * (referential) constraint violation.
     * 
     * @param id ID of the entity to be deleted.
     */
    void delete(long id);

}
