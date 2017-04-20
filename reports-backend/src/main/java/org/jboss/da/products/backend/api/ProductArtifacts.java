package org.jboss.da.products.backend.api;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@EqualsAndHashCode(exclude = "artifacts")
public class ProductArtifacts {

    private final Product product;

    private final Set<Artifact> artifacts;
}
