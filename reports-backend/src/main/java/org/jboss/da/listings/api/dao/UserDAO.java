package org.jboss.da.listings.api.dao;

import java.util.Optional;

import org.jboss.da.listings.api.model.User;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * 
 */
public interface UserDAO extends GenericDAO<User> {

    /**
     * Finds user by given username.
     * 
     * @param username username of the user
     * @return Optional of user or empty when not found.
     */
    Optional<User> findUser(String username);
}
