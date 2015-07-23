package org.jboss.da.communication.aprox.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "metadata")
public class VersionResponse {

    private String groupId;

    private String artifactId;

    private String version;

    private Versioning versioning;

    public String getGroupId() {
        return groupId;
    }

    @XmlElement
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @XmlElement
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    @XmlElement
    public void setVersion(String version) {
        this.version = version;
    }

    public Versioning getVersioning() {
        return versioning;
    }

    @XmlElement
    public void setVersioning(Versioning versioning) {
        this.versioning = versioning;
    }

    @Override
    public String toString() {
        return "VersionResponse [groupId=" + groupId + ", artifactId=" + artifactId + ", version="
                + version + "]" + versioning;
    }

}
