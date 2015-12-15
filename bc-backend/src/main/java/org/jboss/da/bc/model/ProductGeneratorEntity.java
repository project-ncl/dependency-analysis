package org.jboss.da.bc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.da.communication.model.GAV;
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

    public ProductGeneratorEntity(SCMLocator scm, String name, GAV gav, String productVersion) {
        super(scm, name, gav);

        this.productVersion = productVersion;
    }

    public static EntityConstructor<ProductGeneratorEntity> getConstructor(final String version){
        return (s, n, g) -> new ProductGeneratorEntity(s, n, g, version);
    }
}
