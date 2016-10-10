package org.jboss.da.communication.aprox.model;

import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.ToString;

/**
 * This entity is deserialized from XML file
 * @author sknot
 */
@ToString
public class Versioning {

    @Getter
    @XmlElement(name = "latest")
    private String latestVersion;

    @Getter
    @XmlElement(name = "release")
    private String latestRelease;

    @Getter
    @XmlElement
    private Versions versions;

}
