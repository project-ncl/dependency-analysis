package org.jboss.da.communication.pom.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MavenSCM {

    @Getter
    @XmlElement(name = "url")
    private String url;

    @Getter
    @XmlElement(name = "tag")
    private String tag;
}
