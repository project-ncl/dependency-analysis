package org.jboss.da.communication.pom.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "project", namespace = "http://maven.apache.org/POM/4.0.0")
public class MavenProject {

    @XmlElement(name = "repositories")
    private MavenRepositories mavenRepositories;

    public List<MavenRepository> getMavenRepositories() {
        if (mavenRepositories == null)
            return null;

        return mavenRepositories.getRepositories();
    }
}
