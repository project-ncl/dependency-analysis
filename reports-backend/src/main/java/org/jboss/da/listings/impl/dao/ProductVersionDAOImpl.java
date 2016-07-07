package org.jboss.da.listings.impl.dao;

import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.Product;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.model.ProductSupportStatus;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Stateless
public class ProductVersionDAOImpl extends GenericDAOImpl<ProductVersion> implements
        ProductVersionDAO {

    public ProductVersionDAOImpl() {
        super(ProductVersion.class);
    }

    @Override
    public boolean changeProductVersionStatus(String name, String version,
            ProductSupportStatus newStatus) {
        Optional<ProductVersion> pv = findProductVersion(name, version);
        if (!pv.isPresent())
            return false;
        pv.get().setSupport(newStatus);
        update(pv.get());
        return true;
    }

    @Override
    public Optional<ProductVersion> findProductVersion(String name, String version) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<ProductVersion> cq = cb.createQuery(type);
            Root<ProductVersion> productVersion = cq.from(type);
            Join<ProductVersion, Product> product = productVersion.join("product");
            cq.select(productVersion).where(
                    cb.and(cb.equal(product.get("name"), name),
                            cb.equal(productVersion.get("productVersion"), version)));
            TypedQuery<ProductVersion> q = em.createQuery(cq);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ProductVersion> findProductVersionsWithProduct(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersion> cq = cb.createQuery(type);
        Root<ProductVersion> productVersion = cq.from(type);
        Join<ProductVersion, Product> product = productVersion.join("product");
        cq.select(productVersion).where(cb.equal(product.get("name"), name));
        TypedQuery<ProductVersion> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public List<ProductVersion> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersion> cq = cb.createQuery(type);
        Root<ProductVersion> productVersion = cq.from(type);
        cq.select(productVersion);
        TypedQuery<ProductVersion> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public List<ProductVersion> findProductVersionsWithArtifact(String groupId, String artifactId,
            String version, boolean preciseVersion) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersion> cq = cb.createQuery(type);
        Root<ProductVersion> productVersion = cq.from(type);
        Root<WhiteArtifact> artifact = cq.from(WhiteArtifact.class);
        Join<Artifact, GA> ga = artifact.join("ga");
        if (!preciseVersion)
            version += '%';
        cq.select(productVersion).where(
                cb.and(cb.equal(ga.get("groupId"), groupId),
                        cb.equal(ga.get("artifactId"), artifactId),
                        cb.or(cb.like(artifact.get("version"), version),
                                cb.like(artifact.get("osgiVersion"), version))));
        TypedQuery<ProductVersion> q = em.createQuery(cq);
        List<ProductVersion> list = q.getResultList();
        return list;
    }

    @Override
    public List<ProductVersion> findProductVersions(Long id, String name, String version,
            ProductSupportStatus status) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersion> cq = cb.createQuery(type);
        Root<ProductVersion> productVersion = cq.from(type);
        Join<ProductVersion, Product> product = productVersion.join("product");
        List<Predicate> predicates = new ArrayList<>();
        if (id != null) {
            predicates.add(cb.equal(productVersion.get("id"), id));
        }
        if (name != null) {
            predicates.add(cb.equal(product.get("name"), name));
        }
        if (version != null) {
            predicates.add(cb.equal(productVersion.get("productVersion"), version));
        }
        if (status != null) {
            predicates.add(cb.equal(productVersion.get("support"), status));
        }
        cq.select(productVersion).where(
                cb.and(cb.and(predicates.toArray(new Predicate[predicates.size()]))));
        TypedQuery<ProductVersion> q = em.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public List<ProductVersion> findProductVersionsWithArtifactsByStatus(ProductSupportStatus status) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersion> cq = cb.createQuery(type);
        Root<ProductVersion> productVersion = cq.from(ProductVersion.class);
        cq.select(productVersion).where(cb.and(cb.equal(productVersion.get("support"), status)));
        TypedQuery<ProductVersion> q = em.createQuery(cq);
        List<ProductVersion> pv = q.getResultList();
        return pv;
    }

    @Override
    public List<ProductVersionArtifactRelationship> findProductVersionsWithArtifactByGAV(
            String groupId, String artifactId, String version) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersionArtifactRelationship> cq = cb
                .createQuery(ProductVersionArtifactRelationship.class);
        Root<ProductVersion> productVersion = cq.from(type);
        Root<WhiteArtifact> artifact = cq.from(WhiteArtifact.class);
        Join<WhiteArtifact, GA> ga = artifact.join("ga");
        Expression<Collection<WhiteArtifact>> artifacts = productVersion.get("whiteArtifacts");
        cq.multiselect(productVersion, artifact);
        cq.where(cb.and(
                cb.isMember(artifact, artifacts),
                cb.equal(ga.get("artifactId"), artifactId),
                cb.equal(ga.get("groupId"), groupId),
                cb.or(cb.equal(artifact.get("version"), version),
                        cb.equal(artifact.get("osgiVersion"), version))));
        TypedQuery<ProductVersionArtifactRelationship> q = em.createQuery(cq);
        List<ProductVersionArtifactRelationship> l = q.getResultList();
        return l;
    }

    @Override
    public List<ProductVersionArtifactRelationship> findProductVersionsWithArtifactsByGAStatus(
            String groupId, String artifactId, Optional<ProductSupportStatus> status) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductVersionArtifactRelationship> cq = cb
                .createQuery(ProductVersionArtifactRelationship.class);
        Root<ProductVersion> productVersion = cq.from(type);
        Root<WhiteArtifact> artifact = cq.from(WhiteArtifact.class);
        Join<WhiteArtifact, GA> ga = artifact.join("ga");
        Expression<Collection<WhiteArtifact>> artifacts = productVersion.get("whiteArtifacts");
        cq.multiselect(productVersion, artifact);

        Predicate restriction = cb.and(
                cb.isMember(artifact, artifacts),
                cb.equal(ga.get("artifactId"), artifactId),
                cb.equal(ga.get("groupId"), groupId));
        status.ifPresent(x -> {cb.and(restriction, cb.equal(productVersion.get("support"), x));});

        cq.where(restriction);
        TypedQuery<ProductVersionArtifactRelationship> q = em.createQuery(cq);
        List<ProductVersionArtifactRelationship> l = q.getResultList();
        return l;
    }
}
