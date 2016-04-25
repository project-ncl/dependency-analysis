package org.jboss.da.bc.model.backend;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ToString
public class ProductGeneratorEntity extends GeneratorEntity {

    @Getter
    @Setter
    String productVersion;

    public ProductGeneratorEntity(SCMLocator scm, int id, GAV gav, String productVersion) {
        super(scm, id, gav);

        this.productVersion = productVersion;
    }

    public static EntityConstructor<ProductGeneratorEntity> getConstructor(final String version){
        return (s, n, g) -> new ProductGeneratorEntity(s, n, g, version);
    }
}
