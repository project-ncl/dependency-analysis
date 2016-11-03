package org.jboss.da.products.api;

import java.util.Set;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ProductsService {

    Set<ArtifactDiff> difference(long leftProduct, long rightProduct);
}
