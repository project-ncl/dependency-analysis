package org.jboss.da.model.rest;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class GA {

    private static final Pattern GROUP_ID_PATTERN = Pattern
            .compile("([a-zA-Z_][a-zA-Z\\d_]*\\.)*[a-zA-Z_][a-zA-Z\\d_]*");

    private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("[a-zA-Z0-9_.-]+");

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

    public boolean isValid() {
        return GROUP_ID_PATTERN.matcher(groupId).matches()
                && ARTIFACT_ID_PATTERN.matcher(artifactId).matches();
    }

}
