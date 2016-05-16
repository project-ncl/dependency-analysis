package org.jboss.da.products.api;

import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface ProductsService {

    Set<ArtifactDiff> difference(long leftProduct, long rightProduct);
}
