package org.jboss.da.communication.pom.model;

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
    @XmlElement(name = "parent")
    private MavenParent parent;

    @XmlElement(name = "groupId")
    private String groupId;

    @Getter
    @XmlElement(name = "artifactId", required = true)
    private String artifactId;

    @XmlElement(name = "version")
    private String version;

    @Getter
    @XmlElement(name = "name")
    private String name;

    @Getter
    @XmlElement(name = "scm")
    private MavenSCM scm;

    @XmlElement(name = "repositories")
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
}
