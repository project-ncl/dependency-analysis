package org.jboss.da.listings.api.dao;

import java.util.List;
import org.jboss.da.listings.api.model.WhiteArtifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public interface WhiteArtifactDAO extends ArtifactDAO<WhiteArtifact> {

    public List<WhiteArtifact> findRedhatArtifact(String groupId, String artifactId, String version);

}
