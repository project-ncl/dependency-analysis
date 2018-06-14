package org.jboss.da.products.api;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class NPMArtifact implements Artifact {

    private final String name;

    private final String version;

    @Override
    public String getIdentifier() {
        return name + ':' + version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ArtifactType getType() {
        return ArtifactType.NPM;
    }
}
