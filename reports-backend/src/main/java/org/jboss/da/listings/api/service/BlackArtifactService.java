package org.jboss.da.listings.api.service;

import java.util.Optional;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.model.BlackArtifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public interface BlackArtifactService extends ArtifactService<BlackArtifact> {

    /**
     * Add artifact to blacklist.
     * If the version contains redhat suffix it is removed. Then the version is converted to OSGi
     * version. It also removes all redhat suffixed artifacts (with given groupId, artifactId and
     * OSGi verison) from whitelist.
     */
    public ArtifactStatus addArtifact(String groupId, String artifactId, String version);

    /**
     * Checks if blacklist contains artifact with specific groupId, artifactId and version.
     * If the version have redhat suffix it is removed. Then the version is converted to OSGi
     * version and finds this version in blacklist;
     * @return found artifact
     */
    public Optional<BlackArtifact> getArtifact(String groupId, String artifactId, String version);

    /**
     * Checks if blacklist contains artifact with specific groupId, artifactId and version.
     * If the version have redhat suffix it is removed. Then the version is converted to OSGi
     * version and finds this version in blacklist;
     *
     * @return found artifact
     */
    public Optional<BlackArtifact> getArtifact(GAV gav);

    /**
     * Remove artifact from list.
     * Removes only exact match of artifact.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return True if artifact was deleted.
     */
    public boolean removeArtifact(String groupId, String artifactId, String version);
}
