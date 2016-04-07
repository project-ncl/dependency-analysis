package org.jboss.da.listings.impl.service;

import org.jboss.da.listings.api.dao.ProductDAO;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.Product;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.ProductService;
import org.jboss.da.listings.model.ProductSupportStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    @Inject
    private ProductDAO productDAO;

    @Inject
    private ProductVersionDAO productVersionDAO;

    @Override
    public boolean addProduct(String name, String version, ProductSupportStatus status) {
        if (productVersionDAO.findProductVersion(name, version).isPresent()) {
            return false;
        }
        Optional<Product> p = productDAO.findProduct(name);
        if (!p.isPresent()) {
            Product pr = new Product(name);
            productDAO.create(pr);
            p = productDAO.findProduct(name);
        }
        productVersionDAO.create(new ProductVersion(p.get(), version, status));
        return true;
    }

    @Override
    public boolean removeProduct(String name, String version) throws EntityNotFoundException {
        Optional<ProductVersion> pv = productVersionDAO.findProductVersion(name, version);
        if (pv.isPresent()) {
            productVersionDAO.delete(pv.get());
            if (productVersionDAO.findProductVersionsWithProduct(name).isEmpty()) {
                productDAO.delete(productDAO.findProduct(name).get());
            }
            return true;
        }
        throw new EntityNotFoundException("Product with this name and version is not in database");
    }

    @Override
    public List<Product> getAll() {
        return productDAO.findAll();
    }

    @Override
    public boolean changeProductStatus(String name, String version, ProductSupportStatus newStatus) {
        return productVersionDAO.changeProductVersionStatus(name, version, newStatus);
    }
}
