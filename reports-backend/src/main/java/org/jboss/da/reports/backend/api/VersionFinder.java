package org.jboss.da.reports.backend.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.VersionLookupResult;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
public interface VersionFinder {

    /**
     * Finds all Red Hat built artifacts (with suffix -redhat) with the same GA
     * 
     * @param gav GroupId and ArtifactId, which specifies the artifact
     * @return Found built Red Hat artifacts with the same GA
     * @throws CommunicationException when there is a problem with communication with remote services
     */
    List<String> getBuiltVersionsFor(GAV gav) throws CommunicationException;

    /**
     * Finds all Red Hat built artifacts (with suffix -redhat) with the same GA and also 
     * the best match built artifact to the requested GA
     * 
     * @param gav GroupId and ArtifactId, which specifies the artifact
     * @return Found data about built artifacts
     * @throws CommunicationException when there is a problem with communication with remote services
     */
    VersionLookupResult lookupBuiltVersions(GAV gav) throws CommunicationException;

    /**
     * Tries to find the Red Hat built version of specified artifacts. Tries to find
     * the latest built. If there is not built artifact with given GAV, empty Optional is returned.
     * 
     * @param gav GAV, which specifies the artifact
     * @return Found biggest version of built artifact with given GAV or empty Optional if this artifact was not built yet
     * @throws CommunicationException when there is a problem with communication with remote services
     */
    Optional<String> getBestMatchVersionFor(GAV gav) throws CommunicationException;

    /**
     * Tries to find the Red Hat built version of specified artifacts in the provided list of available built versions of artifact.
     * Tries to find the latest built. If there is not built artifact with given GAV, empty Optional is returned.
     *
     * @param gav GAV, which specifies the artifact
     * @param availableVersions Available built versions of the specified artifact
     * @return Found biggest version of built artifact with given GAV or empty Optional if this artifact was not built yet
     */
    Optional<String> getBestMatchVersionFor(GAV gav, List<String> availableVersions);

}
