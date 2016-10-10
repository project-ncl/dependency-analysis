package org.jboss.da.communication.aprox.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.ToString;

/**
 * This entity is deserialized from XML file
 * @author sknot
 */
@XmlRootElement(name = "metadata")
@ToString
public class VersionResponse {

    @Getter
    @XmlElement
    private String groupId;

    @Getter
    @XmlElement
    private String artifactId;

    @Getter
    @XmlElement
    private String version;

    @Getter
    @XmlElement
    private Versioning versioning;

}
