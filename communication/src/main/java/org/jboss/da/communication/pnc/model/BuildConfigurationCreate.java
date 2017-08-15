package org.jboss.da.communication.pnc.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    private RepositoryConfiguration repositoryConfiguration;

    @Getter
    @Setter
    private String scmRevision;

    private Date creationTime;

    private Date lastModificationTime;

    @Getter
    @Setter
    private Project project;

    @Getter
    @Setter
    private IdWrapper environment;

    @Getter
    @Setter
    private List<Integer> dependencyIds = new ArrayList<>();

    @Getter
    @Setter
    private Integer productVersionId;

    @Getter
    @Setter
    private boolean archived;

    @Getter
    @Setter
    private Map<String, String> genericParameters = new HashMap<>();

    public void setEnvironmentId(int id) {
        IdWrapper env = new IdWrapper();
        env.setId(id);
        this.environment = env;
    }

    public void setProjectId(int id) {
        Project proj = new Project();
        proj.setId(id);
        this.project = proj;
    }

    public void setRepositoryId(int id) {
        this.repositoryConfiguration = RepositoryConfiguration.builder().id(id).build();
    }

    public Date getCreationTime() {
        return creationTime == null ? null : new Date(creationTime.getTime());
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime == null ? null : new Date(creationTime.getTime());
    }

    public Date getLastModificationTime() {
        return lastModificationTime == null ? null : new Date(lastModificationTime.getTime());
    }

    public void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime == null ? null : new Date(
                lastModificationTime.getTime());
    }
}
