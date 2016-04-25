package org.jboss.da.model.rest;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlTransient;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@EqualsAndHashCode
public class GAV {

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
}
