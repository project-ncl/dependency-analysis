package org.jboss.da.products.model.rest;

import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.DummyVersionComparator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author jbrazdil
 */
@EqualsAndHashCode
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
    public GADiff(
            @JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId,
            @JsonProperty("leftVersion") String leftVersion,
            @JsonProperty("rightVersion") String rightVersion,
            @JsonProperty("differenceType") String differenceType) {
        this.ga = new GA(groupId, artifactId);
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
        this.differenceType = differenceType;
    }

    public GADiff(GA ga, String leftVersion, String rightVersion, String differenceType) {
        this.ga = ga;
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
        this.differenceType = differenceType;
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
    public int compareTo(GADiff o) {
        int gaCmp = this.ga.compareTo(o.ga);
        if (gaCmp != 0) {
            return gaCmp;
        }
        gaCmp = DummyVersionComparator.compareVersions(this.leftVersion, o.leftVersion);
        if (gaCmp != 0) {
            return gaCmp;
        }
        return DummyVersionComparator.compareVersions(this.rightVersion, o.rightVersion);
    }

}
