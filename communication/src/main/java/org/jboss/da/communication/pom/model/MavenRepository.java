package org.jboss.da.communication.pom.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import lombok.Getter;

@XmlAccessorType(XmlAccessType.FIELD)
public class MavenRepository {

    @Getter
    @XmlElement(name = "id", namespace = MavenProject.NAMESPACE)
    private String id;

    @Getter
    @XmlElement(name = "url", namespace = MavenProject.NAMESPACE)
    private String url;
}
