package org.jboss.da.listings.impl.dao;

import org.jboss.da.listings.api.dao.ProductDAO;
import org.jboss.da.listings.api.model.Product;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

@Stateless
public class ProductDAOImpl extends GenericDAOImpl<Product> implements ProductDAO {

    public ProductDAOImpl() {
        super(Product.class);
    }

    @Override
    public List<Product> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(type);
        Root<Product> product = cq.from(type);
        cq.select(product);
        TypedQuery<Product> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public Optional<Product> findProduct(String name) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(type);
            Root<Product> product = cq.from(type);
            cq.select(product).where(cb.equal(product.get("name"), name));
            TypedQuery<Product> q = em.createQuery(cq);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
