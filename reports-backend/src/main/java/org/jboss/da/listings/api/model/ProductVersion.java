package org.jboss.da.listings.api.model;

import org.jboss.da.listings.api.service.ArtifactService.SupportStatus;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@NoArgsConstructor
@Entity
public class ProductVersion extends GenericEntity {

    @Setter
    @Getter
    @ManyToOne
    private Product product;

    @Setter
    @Getter
    @NonNull
    private String productVersion;

    @Setter
    @Getter
    private SupportStatus support;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<WhiteArtifact> whiteArtifacts;

    public ProductVersion(Product p, String productVersion, SupportStatus support) {
        this.product = p;
        this.productVersion = productVersion;
        this.support = support;
        whiteArtifacts = new HashSet<>();
    }

    public void addArtifact(WhiteArtifact artifact) {
        whiteArtifacts.add(artifact);
    }

    public void removeArtifact(WhiteArtifact artifact) {
        whiteArtifacts.remove(artifact);
    }
}
