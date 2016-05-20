package org.jboss.da.reports.api;

import org.jboss.da.model.rest.GAV;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class BuiltReportModule {

    public BuiltReportModule(GAV gav) {
        this.groupId = gav.getGroupId();
        this.artifactId = gav.getArtifactId();
        this.version = gav.getVersion();
    }

    @Getter
    private String groupId;

    @Getter
    private String artifactId;

    @Getter
    private String version;

    @Getter
    @Setter
    private String builtVersion;

    @Getter
    @Setter
    private Set<String> availableVersions = new HashSet<>();

}
