package org.jboss.da.products.api;

import org.jboss.da.model.rest.GAV;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class MavenArtifact implements Artifact {

    private final GAV gav;

    @Override
    public String getIdentifier() {
        return gav.toString();
    }

    @Override
    public String getName() {
        return gav.getGA().toString();
    }

    @Override
    public String getVersion() {
        return gav.getVersion();
    }

    @Override
    public ArtifactType getType() {
        return ArtifactType.MAVEN;
    }
}
