package org.jboss.da.communication.pom.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MavenSCM {

    @Getter
    @XmlElement(name = "url", namespace = MavenProject.NAMESPACE)
    private String url;

    @Getter
    @XmlElement(name = "tag", namespace = MavenProject.NAMESPACE)
    private String tag;
}
