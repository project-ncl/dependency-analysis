package org.jboss.da.products.model.rest;

import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.VersionComparator;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author jbrazdil
 */
public class GADiff implements Comparable<GADiff> {

    @NonNull
    private final GA ga;

    @Getter
    @NonNull
    private final String leftVersion;

    @Getter
    @NonNull
    private final String rightVersion;

    @Getter
    @NonNull
    private final String differenceType;

    @JsonCreator
    public GADiff(@JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId,
            @JsonProperty("leftVersion") String leftVersion,
            @JsonProperty("rightVersion") String rightVersion,
            @JsonProperty("differenceType") String differenceType) {
        this.ga = new GA(groupId, artifactId);
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
        this.differenceType = VersionComparator.difference(leftVersion, rightVersion).toString();
    }

    public GADiff(GA ga, String leftVersion, String rightVersion) {
        this.ga = ga;
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
        this.differenceType = VersionComparator.difference(leftVersion, rightVersion).toString();
    }

    @JsonIgnore
    //@XmlTransient
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
    public int compareTo(GADiff o) {
        int gaCmp = this.ga.compareTo(o.ga);
        if (gaCmp != 0) {
            return gaCmp;
        }
        gaCmp = VersionComparator.compareVersions(this.leftVersion, o.leftVersion);
        if (gaCmp != 0) {
            return gaCmp;
        }
        return VersionComparator.compareVersions(this.rightVersion, o.rightVersion);
    }

}
