package org.jboss.da.rest.reports.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.rest.listings.model.RestProductInput;

import javax.xml.bind.annotation.XmlTransient;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public class LookupReport {

    /*
     * Manually unwrap 'gav' via getters so as not to confuse Swagger.
     * 
     * Also, use @JsonIgnore so that the getters and setters generated for 'gav' are also ignored by Jackson.
     */
    @Getter
    @Setter
    @NonNull
    @JsonIgnore
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
    private List<RestProductInput> whitelisted;

    // **************************************************************************
    // Keep `getGroupId`, `getArtifactId`, and `getVersion` here for Swagger,
    // and so that Jackson knows how to marshall 'gav' properly!
    // **************************************************************************
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
