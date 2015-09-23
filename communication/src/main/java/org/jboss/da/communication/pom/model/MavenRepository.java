package org.jboss.da.communication.pom.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;

@XmlAccessorType(XmlAccessType.FIELD)
public class MavenRepository {

    @Getter
    @XmlElement(name = "id")
    private String id;

    @Getter
    @XmlElement(name = "url")
    private String url;
}
