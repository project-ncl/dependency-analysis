package org.jboss.da.listings.api.service;

import java.util.List;

import org.jboss.da.listings.api.model.Artifact;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface ArtifactService<T extends Artifact> {

    public enum SortBy {
        GAV, SUPPORT, PRODUCT;
    };

    public enum ArtifactStatus {
        ADDED, NOT_MODIFIED, IS_BLACKLISTED, WAS_WHITELISTED
    };

    /**
     * Finds and return all artifacts.
     * 
     * @return List of found artifacts.
     */
    List<T> getAll();

}
