package org.jboss.da.reports.backend.api;

import org.jboss.da.communication.aprox.model.GAV;

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
     * @return Found built RedHat artifacts with the same GA
     */
    List<String> getVersionsFor(GAV gav);

    /**
     * Tries to find the RedHat built version of specified artifacts. Tries to find
     * the latest built. If there is not built artifact with given GAV, null is returned.
     * 
     * @param gav GAV, which specifies the artifact
     * @return Found biggest version of built artifact with figen GAV or null if this artifact was not built yet
     */
    String getBestMatchVersionFor(GAV gav);
}
