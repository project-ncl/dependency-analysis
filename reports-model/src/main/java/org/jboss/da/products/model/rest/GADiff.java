package org.jboss.da.products.model.rest;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jboss.da.model.rest.GA;

import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author jbrazdil
 */
public class GADiff {

    @NonNull
    private final GA ga;

    @Getter
    @NonNull
    private final String leftVersion;

    @Getter
    @NonNull
    private final String rightVersion;

    @JsonCreator
    public GADiff(@JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId,
            @JsonProperty("leftVersion") String leftVersion,
            @JsonProperty("rightVersion") String rightVersion) {
        this.ga = new GA(groupId, artifactId);
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
    }

    public GADiff(GA ga, String leftVersion, String rightVersion) {
        this.ga = ga;
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
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

}
