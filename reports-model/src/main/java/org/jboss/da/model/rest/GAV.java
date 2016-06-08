package org.jboss.da.model.rest;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@EqualsAndHashCode
public class GAV implements Comparable<GAV> {

    @NonNull
    private final GA ga;

    @Getter
    @NonNull
    private final String version;

    @JsonCreator
    public GAV(@JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId, @JsonProperty("version") String version) {
        this.ga = new GA(groupId, artifactId);
        this.version = version;
    }

    @JsonIgnore
    @XmlTransient
    /*
     * The @XmlTransient annotation is added so that Swagger doesn't try to represent the 'GA' object in its model schema
     */
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
            return (this.version.compareTo(o.version));
        } else
            return gaCmp;

    }
}
