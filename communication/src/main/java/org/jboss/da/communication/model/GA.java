package org.jboss.da.communication.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class GA {

    @Getter
    @NonNull
    private final String groupId;

    @Getter
    @NonNull
    private final String artifactId;

    @JsonCreator
    public GA(@JsonProperty("groupId") String groupId, @JsonProperty("artifactId") String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId;
    }

}
