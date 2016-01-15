package org.jboss.da.listings.impl.service;

import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.service.ArtifactService.SupportStatus;
import org.jboss.da.listings.api.service.ProductVersionService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductVersionServiceImpl implements ProductVersionService {

    @Inject
    private ProductVersionDAO productVersionDAO;

    @Override
    public List<ProductVersion> getAll() {
        return productVersionDAO.findAll();
    }

    @Override
    public Optional<ProductVersion> getProductVersion(String name, String version) {
        return productVersionDAO.findProductVersion(name, version);
    }

    @Override
    public List<ProductVersion> getProductVersionsOfArtifact(String groupId, String artifactId,
            String version) {
        return productVersionDAO.findProductVersionsWithArtifact(groupId, artifactId, version);
    }

    @Override
    public List<ProductVersion> getProductVersions(Long id, String name, String version,
            SupportStatus status) {
        return productVersionDAO.findProductVersions(id, name, version, status);
    }

    @Override
    public List<ProductVersion> getProductVersionsWithArtifactsByStatus(SupportStatus status) {
        return productVersionDAO.findProductVersionsWithArtifactsByStatus(status);
    }

    @Override
    public List<ProductVersionArtifactRelationship> getProductVersionsWithArtifactByGAV(
            String groupId, String artifactId, String version) {
        return productVersionDAO.findProductVersionsWithArtifactByGAV(groupId, artifactId, version);
    }

    @Override
    public List<ProductVersionArtifactRelationship> getProductVersionsWithArtifactsByGAStatus(
            String groupId, String artifactId, SupportStatus status) {
        return productVersionDAO.findProductVersionsWithArtifactsByGAStatus(groupId, artifactId,
                Optional.of(status));
    }

    @Override
    public List<ProductVersionArtifactRelationship> getProductVersionsWithArtifactsByGA(
            String groupId, String artifactId) {
        return productVersionDAO.findProductVersionsWithArtifactsByGAStatus(groupId, artifactId,
                Optional.empty());
    }
}
