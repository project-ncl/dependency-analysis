package org.jboss.da.reports.backend.api;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;

import java.util.List;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
public interface VersionFinder {

    /**
     * Finds all RedHat built artifacts (with suffix -redhat) with the same GA
     * 
     * @param gav GroupId and ArtifactId, which specifies the artifact
     * @return Found built RedHat artifacts with the same GA or null if the GA is not found
     * @throws CommunicationException when there is a problem with communication with remote services.
     */
    List<String> getVersionsFor(GAV gav) throws CommunicationException;

    /**
     * Tries to find the RedHat built version of specified artifacts. Tries to find
     * the latest built. If there is not built artifact with given GAV, null is returned.
     * 
     * @param gav GAV, which specifies the artifact
     * @return Found biggest version of built artifact with given GAV or null if this artifact was not built yet
     * @throws CommunicationException when there is a problem with communication with remote services.
     */
    String getBestMatchVersionFor(GAV gav) throws CommunicationException;

    /**
     * Tries to find the RedHat built version of specified artifacts in the provided list of available built versions of artifact.
     * Tries to find the latest built. If there is not built artifact with given GAV, null is returned.
     *
     * @param gav GAV, which specifies the artifact
     * @param availableVersions Available built versions of the specified artifact
     * @return Found biggest version of built artifact with given GAV or null if this artifact was not built yet
     */
    String getBestMatchVersionFor(GAV gav, List<String> availableVersions);
}
