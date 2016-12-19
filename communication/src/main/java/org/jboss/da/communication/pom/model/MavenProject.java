package org.jboss.da.communication.pom.model;

import org.jboss.da.model.rest.GAV;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import lombok.Getter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "project", namespace = MavenProject.NAMESPACE)
public class MavenProject {

    public final static String NAMESPACE = "http://maven.apache.org/POM/4.0.0";

    @Getter
    @XmlElement(name = "parent", namespace = MavenProject.NAMESPACE)
    private MavenParent parent;

    @XmlElement(name = "groupId", namespace = MavenProject.NAMESPACE)
    private String groupId;

    @Getter
    @XmlElement(name = "artifactId", required = true, namespace = MavenProject.NAMESPACE)
    private String artifactId;

    @XmlElement(name = "version", namespace = MavenProject.NAMESPACE)
    private String version;

    @Getter
    @XmlElement(name = "name", namespace = MavenProject.NAMESPACE)
    private String name;

    @Getter
    @XmlElement(name = "scm", namespace = MavenProject.NAMESPACE)
    private MavenSCM scm;

    @XmlElement(name = "repositories", namespace = MavenProject.NAMESPACE)
    private MavenRepositories mavenRepositories;

    public List<MavenRepository> getMavenRepositories() {
        if (mavenRepositories == null)
            return null;

        return mavenRepositories.getRepositories();
    }

    public String getGroupId() {
        if (groupId != null)
            return groupId;
        else
            return parent.getGroupId();
    }

    public String getVersion() {
        if (version != null)
            return version;
        else
            return parent.getVersion();
    }

    public GAV getGAV() {
        return new GAV(getGroupId(), getArtifactId(), getVersion());
    }
}
