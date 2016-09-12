package org.jboss.da.listings.api.service;

import java.util.List;

import org.jboss.da.listings.api.model.Artifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public interface ArtifactService<T extends Artifact> {

    public enum SortBy {
        GAV, SUPPORT, PRODUCT;
    };

    public enum ArtifactStatus {
        ADDED, NOT_MODIFIED, IS_BLACKLISTED, WAS_WHITELISTED, GA_EXISTS
    };

    /**
     * Finds and return all artifacts.
     * 
     * @return List of found artifacts.
     */
    List<T> getAll();

}
