package org.jboss.da.communication.pnc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationBPMCreate {

    @Getter
    @Setter
    @NonNull
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String buildScript;

    @Getter
    @Setter
    private String scmRepoURL;

    @Getter
    @Setter
    private String scmRevision;

    @Getter
    @Setter
    private String scmExternalRepoURL;

    @Getter
    @Setter
    private String scmExternalRevision;

    @Getter
    @Setter
    private Integer projectId;

    @Getter
    @Setter
    private Integer buildEnvironmentId;

    @Getter
    @Setter
    private List<Integer> dependencyIds = new ArrayList<>();

    @Getter
    @Setter
    private Set<Integer> buildConfigurationSetIds = new HashSet<>();

    @Getter
    @Setter
    private Integer productVersionId;

    @Getter
    @Setter
    private Map<String, String> genericParameters = new HashMap<>();

    public void setEnvironmentId(int id) {
        this.buildEnvironmentId = id;
    }
}
