package org.jboss.da.model.rest;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern pattern = Pattern.compile(
            "^(?<major>[0-9]{1,9})?(\\.(?<minor>[0-9]{1,9})(\\.(?<micro>[0-9]{1,9}))?)?([.-]?(?<qualifier>.+?))?$");
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

    public static int compareVersions(String version1, String version2) {
        Matcher matcher1 = pattern.matcher(version1);
        Matcher matcher2 = pattern.matcher(version2);
        if (!matcher1.matches() || !matcher2.matches()) {
            throw new IllegalArgumentException("Version " + version1 + " or " + version2 + " is unparsable");
        }
        int major1 = parseNumberString(matcher1.group("major"));
        int minor1 = parseNumberString(matcher1.group("minor"));
        int micro1 = parseNumberString(matcher1.group("micro"));
        String qualifier1 = matcher1.group("qualifier") == null ? "" : matcher1.group("qualifier");

        int major2 = parseNumberString(matcher2.group("major"));
        int minor2 = parseNumberString(matcher2.group("minor"));
        int micro2 = parseNumberString(matcher2.group("micro"));
        String qualifier2 = matcher2.group("qualifier") == null ? "" : matcher2.group("qualifier");

        int r = Integer.compare(major1, major2);
        if (r != 0) {
            return r;
        }
        r = Integer.compare(minor1, minor2);
        if (r != 0) {
            return r;
        }
        r = Integer.compare(micro1, micro2);
        if (r != 0) {
            return r;
        }

        String[] tokens1 = qualifier1.split("[.-]");
        String[] tokens2 = qualifier2.split("[.-]");

        for (int i = 0; i < tokens1.length && i < tokens2.length; i++) {
            try {
                int num1 = Integer.parseInt(tokens1[i]);
                int num2 = Integer.parseInt(tokens2[i]);
                r = Integer.compare(num1, num2);
            } catch (NumberFormatException ex) {
                r = tokens1[i].compareTo(tokens2[i]);
            }
            if (r != 0) {
                return r;
            }
        }
        return tokens1.length - tokens2.length;
    }

    private static int parseNumberString(String segmentString) {
        return segmentString == null ? 0 : Integer.parseInt(segmentString);
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
            return compareVersions(this.version, o.version);
        } else {
            return gaCmp;
        }
    }
}
