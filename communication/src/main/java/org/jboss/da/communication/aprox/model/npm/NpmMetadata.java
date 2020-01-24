package org.jboss.da.communication.aprox.model.npm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmMetadata {

    private final String name;

    private final Map<String, NpmPackage> versions;

    @JsonCreator
    public NpmMetadata(@JsonProperty("name") String name, @JsonProperty("versions") Map<String, NpmPackage> versions) {
        this.name = name;
        this.versions = versions;
    }

}
