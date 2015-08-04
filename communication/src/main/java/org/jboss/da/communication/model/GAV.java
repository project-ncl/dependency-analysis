package org.jboss.da.communication.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@NoArgsConstructor
public class GAV {

    public GAV(String groupId, String artifactId, String version) {
        this.ga = new GA(groupId, artifactId);
        this.version = version;
    }

    public GAV(GA ga, String version) {
        this.ga = ga;
        this.version = version;
    }

    @Getter
    @Setter
    private GA ga;

    @Getter
    @Setter
    private String version;

    public void setGroupId(String groupId) {
        ga.setGroupId(groupId);
    }

    public void setArtifactId(String artifactId) {
        ga.setArtifactId(artifactId);
    }

    public String getGroupId() {
        return ga.getGroupId();
    }

    public String getArtifactId() {
        return ga.getArtifactId();
    }

}
