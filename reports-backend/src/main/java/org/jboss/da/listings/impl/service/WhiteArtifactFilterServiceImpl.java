package org.jboss.da.listings.impl.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.api.service.WhiteArtifactFilterService;
import org.jboss.da.listings.model.ProductSupportStatus;

@ApplicationScoped
public class WhiteArtifactFilterServiceImpl implements WhiteArtifactFilterService {

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private ProductVersionService productVersionService;

    @Override
    public List<ProductVersion> toProductsContainingOnlyWhiteArtifacts(List<ProductVersion> products) {
        // Get all black artifacts
        List<BlackArtifact> allBlackArtifacts = blackArtifactService.getAll();

        // Remove every blacklisted artifact from a whitelist
        for (BlackArtifact ba : allBlackArtifacts) {
            for (ProductVersion pv : products) {
                pv.getWhiteArtifacts()
                        .removeIf(x -> x.getGa().equals(ba.getGa()) && x.getVersion().equals(ba.getVersion()));
            }
        }
        return products;
    }

    @Override
    public List<ProductVersionArtifactRelationship> toProductRelsContainingOnlyWhiteArtifacts(
            List<ProductVersionArtifactRelationship> products) {
        List<ProductVersion> productVersions = products.stream()
                .map(ProductVersionArtifactRelationship::getProductVersion)
                .collect(Collectors.toList());

        toProductsContainingOnlyWhiteArtifacts(productVersions);
        return products;
    }

    @Override
    public List<ProductVersion> getAllWithWhiteArtifacts() {
        return toProductsContainingOnlyWhiteArtifacts(productVersionService.getAll());
    }

    @Override
    public Optional<ProductVersion> getProductVersionWithWhiteArtifacts(String name, String version) {
        Optional<ProductVersion> pv = productVersionService.getProductVersion(name, version);
        if (pv.isPresent()) {
            toProductsContainingOnlyWhiteArtifacts(Arrays.asList(pv.get()));
        }
        return pv;
    }

    @Override
    public List<ProductVersion> getProductVersionsWithWhiteArtifactsByStatus(ProductSupportStatus status) {
        return toProductsContainingOnlyWhiteArtifacts(
                productVersionService.getProductVersionsWithArtifactsByStatus(status));
    }

    @Override
    public List<ProductVersionArtifactRelationship> getProductVersionsWithWhiteArtifactsByGAV(
            String groupId,
            String artifactId,
            String version) {
        return toProductRelsContainingOnlyWhiteArtifacts(
                productVersionService.getProductVersionsWithArtifactByGAV(groupId, artifactId, version));
    }

    @Override
    public List<ProductVersionArtifactRelationship> getProductVersionsWithWhiteArtifactsByGAStatus(
            String groupId,
            String artifactId,
            ProductSupportStatus status) {
        return toProductRelsContainingOnlyWhiteArtifacts(
                productVersionService.getProductVersionsWithArtifactsByGAStatus(groupId, artifactId, status));
    }

}
