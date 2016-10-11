package org.jboss.da.communication.pnc.model;

import java.util.List;

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
    private String scmRepoURL;

    @Getter
    private String scmRevision;

    @Getter
    private String scmExternalRepoURL;

    @Getter
    private String scmExternalRevision;

    @Getter
    @Setter
    private Integer projectId;

    @Getter
    @Setter
    private Integer buildEnvironmentId;

    @Getter
    @Setter
    private List<Integer> dependencyIds;

    @Getter
    @Setter
    private Integer productVersionId;

    public void setEnvironmentId(int id) {
        this.projectId = id;
    }

    public void setSCMLocation(String repoURL, String revision) {
        if (repoURL == null) {
            repoURL = "";
        }
        if (repoURL.contains("code-stage.eng.nay.redhat.com")
                || repoURL.contains("pnc-gerrit.pnc.dev.eng.bos.redhat.com")) {
            this.scmRepoURL = repoURL;
            this.scmRevision = revision;
        } else {
            this.scmExternalRepoURL = repoURL;
            this.scmExternalRevision = revision;
        }
    }

}
