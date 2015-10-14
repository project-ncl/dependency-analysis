package org.jboss.da.communication.pnc.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationCreate {

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
    private Date creationTime;

    @Getter
    @Setter
    private Date lastModificationTime;

    @Getter
    @Setter
    private String buildStatus;

    @Getter
    @Setter
    private String repositories;

    @Getter
    @Setter
    private int projectId;

    @Getter
    @Setter
    private int environmentId;

    @Getter
    @Setter
    private List<Integer> dependencyIds;

    @Getter
    @Setter
    private List<Integer> productVersionIds;

}
