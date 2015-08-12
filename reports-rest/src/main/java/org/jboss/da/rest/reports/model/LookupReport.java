package org.jboss.da.rest.reports.model;

import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.jboss.da.communication.model.GAV;

import javax.xml.bind.annotation.XmlTransient;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public class LookupReport {

    @Getter
    @Setter
    @NonNull
    @JsonUnwrapped
    @XmlTransient
    private GAV gav;

    @Getter
    @Setter
    private String bestMatchVersion;

    @Getter
    @Setter
    private List<String> availableVersions;

    @Getter
    @Setter
    private boolean blacklisted;

    @Getter
    @Setter
    private boolean whitelisted;

    public String getGroupId() {
        return gav.getGroupId();
    }

    public String getArtifactId() {
        return gav.getArtifactId();
    }

    public String getVersion() {
        return gav.getVersion();
    }
}
