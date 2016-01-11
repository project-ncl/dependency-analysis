package org.jboss.da.listings.api.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
public class ProductVersionArtifactRelationship {

    @Getter
    @Setter
    private WhiteArtifact artifact;

    @Getter
    @Setter
    private ProductVersion productVersion;

    public ProductVersionArtifactRelationship(ProductVersion productVersion, WhiteArtifact artifact) {
        this.artifact = artifact;
        this.productVersion = productVersion;
    }

}
