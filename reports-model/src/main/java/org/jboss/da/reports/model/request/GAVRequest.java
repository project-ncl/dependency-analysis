package org.jboss.da.reports.model.request;

import org.jboss.da.model.rest.GAV;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@JsonRootName(value = "report")
public class GAVRequest {

    @JsonCreator
    public GAVRequest(@JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId, @JsonProperty("version") String version,
            @JsonProperty("productNames") Set<String> productNames,
            @JsonProperty("productVersionIds") Set<Long> productVersionIds) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.productNames = productNames;
        this.productVersionIds = productVersionIds;
    }

    @Getter
    @NonNull
    private final String groupId;

    @Getter
    @NonNull
    private final String artifactId;

    @Getter
    @NonNull
    private final String version;

    @Getter
    @NonNull
    private final Set<String> productNames;

    @Getter
    @NonNull
    private final Set<Long> productVersionIds;

    public GAV asGavObject() {
        return new GAV(groupId, artifactId, version);
    }
}
