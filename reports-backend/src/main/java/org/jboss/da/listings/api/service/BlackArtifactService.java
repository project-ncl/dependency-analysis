package org.jboss.da.listings.api.service;

import java.util.Optional;

import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

import java.util.Set;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
public interface BlackArtifactService extends ArtifactService<BlackArtifact> {

    /**
     * Checks if list contains artifact with specific groupId, artifactId and version. All restrictions and conversions
     * are applied like using getArtifact method of specific list.
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return True if list contains the artifact otherwise false.
     */
    boolean isArtifactPresent(String groupId, String artifactId, String version);

    /**
     * Checks if list contains artifact with specific GAV. All restrictions and conversions are applied like using
     * getArtifact method of specific list.
     *
     * @param gav
     * @return True if list contains the artifact otherwise false.
     */
    boolean isArtifactPresent(GAV gav);

    /**
     * Add artifact to blacklist. If the version contains redhat suffix it is removed. Then the version is converted to
     * OSGi version. It also removes all redhat suffixed artifacts (with given groupId, artifactId and OSGi verison)
     * from whitelist.
     */
    public ArtifactStatus addArtifact(String groupId, String artifactId, String version);

    /**
     * Checks if blacklist contains artifact with specific groupId, artifactId and version. If the version have redhat
     * suffix it is removed. Then the version is converted to OSGi version and finds this version in blacklist;
     *
     * @return found artifact
     */
    public Optional<BlackArtifact> getArtifact(String groupId, String artifactId, String version);

    /**
     * Checks if blacklist contains artifact with specific groupId, artifactId and version. If the version have redhat
     * suffix it is removed. Then the version is converted to OSGi version and finds this version in blacklist;
     *
     * @return found artifact
     */
    public Optional<BlackArtifact> getArtifact(GAV gav);

    /**
     * Remove artifact from list. Removes only exact match of artifact.
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return True if artifact was deleted.
     */
    public boolean removeArtifact(String groupId, String artifactId, String version);

    public Set<BlackArtifact> getArtifacts(String groupId, String artifactId);

    /**
     * Fetches all artifacts from blocklist, that have GA matching one of the provided GAs.
     *
     * @param gas Set of GAs to find in blocklist.
     * @return Set of blocklisted artifacts.
     */
    Set<GAV> prefetchGAs(Set<GA> gas);

    /**
     * Checks if given GA + version is blocklisted in the provided set of GAVs (obtained using
     * {@link BlackArtifactService#prefetchGAs(Set)}). The same transformations as in
     * {@link BlackArtifactService#getArtifact(GAV)} are performed with the version when looking for the artifact.
     *
     * @param cache Cache of blocklisted GAVs, obtained using {@link BlackArtifactService#prefetchGAs(Set)}
     * @param ga Group Id + Artifact Id to check in the blocklist.
     * @param version Version to check in the blocklist.
     * @return True, if the GA + version is present in the blocklist.
     */
    boolean isBlocklisted(Set<GAV> cache, GA ga, String version);
}
