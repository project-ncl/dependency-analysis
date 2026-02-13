package org.jboss.da.listings.api.service;

import org.jboss.da.listings.model.ProductSupportStatus;

import javax.persistence.EntityNotFoundException;

import java.util.NoSuchElementException;

public interface ProductService {

    /**
     * Adds Product into whitelist products
     * 
     * @param name
     * @param version
     * @param status
     * @return true if operation was successful
     */
    boolean addProduct(String name, String version, ProductSupportStatus status);

    /**
     * Remove Product from whitelist products
     * 
     * @param name
     * @param version
     * @return true if operation was successful
     * @throws EntityNotFoundException
     */
    boolean removeProduct(String name, String version) throws EntityNotFoundException;

    /**
     * Change support status of Product with specific name and version
     * 
     * @param name
     * @param version
     * @param newStatus
     * @return True if change was successful otherwise false
     * @throws NoSuchElementException
     */
    boolean changeProductStatus(String name, String version, ProductSupportStatus newStatus);

}
