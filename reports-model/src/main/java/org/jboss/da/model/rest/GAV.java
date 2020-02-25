package org.jboss.da.model.rest;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@EqualsAndHashCode
@JsonPropertyOrder({ "groupId", "artifactId", "version" })
public class GAV implements Comparable<GAV> {

    @NonNull
    private final GA ga;

    @Getter
    @NonNull
    private final String version;

    @JsonCreator
    public GAV(
            @JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId,
            @JsonProperty("version") String version) {
        this.ga = new GA(groupId, artifactId);
        this.version = Objects.requireNonNull(version);
    }

    @JsonIgnore
    public GA getGA() {
        return ga;
    }

    public String getGroupId() {
        return ga.getGroupId();
    }

    public String getArtifactId() {
        return ga.getArtifactId();
    }

    @Override
    public String toString() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
    }

    @Override
    public int compareTo(GAV o) {
        int gaCmp = this.ga.compareTo(o.ga);
        if (gaCmp == 0) {
            return DummyVersionComparator.compareVersions(this.version, o.version);
        } else {
            return gaCmp;
        }
    }
}
