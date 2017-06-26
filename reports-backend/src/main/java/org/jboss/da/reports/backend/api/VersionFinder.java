package org.jboss.da.reports.backend.api;

import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.reports.api.VersionLookupResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public interface VersionFinder {

    /**
     * Tries to find the Red Hat built version of specified artifacts in the provided list of available built versions of artifact.
     * Tries to find the latest built. If there is not built artifact with given GAV, empty Optional is returned.
     *
     * @param gav GAV, which specifies the artifact
     * @param availableVersions Available built versions of the specified artifact
     * @return Found biggest version of built artifact with given GAV or empty Optional if this artifact was not built yet
     */
    Optional<String> getBestMatchVersionFor(GAV gav, List<String> availableVersions);

    CompletableFuture<VersionLookupResult> getVersionsFor(GAV gav,
            CompletableFuture<Set<ProductArtifacts>> availableArtifacts);

}
