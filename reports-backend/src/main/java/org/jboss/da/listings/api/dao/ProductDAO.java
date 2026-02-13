package org.jboss.da.listings.api.dao;

import org.jboss.da.listings.api.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO extends GenericDAO<Product> {

    /**
     * Finds all Products
     * 
     * @return List of products
     */
    List<Product> findAll();

    /**
     * Finds all products matching the supplied product names.
     *
     * @return List of products as if findProduct() was called once for each name in the list.
     */
    List<Product> findAllWithNames(List<String> productNames);

    /**
     * Finds Product with specific name
     * 
     * @param name
     * @return Optional of product or empty
     */
    Optional<Product> findProduct(String name);

}
