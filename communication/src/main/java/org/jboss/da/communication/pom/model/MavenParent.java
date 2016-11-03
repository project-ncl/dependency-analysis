package org.jboss.da.communication.pom.model;

import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class MavenParent {

    @Getter
    @XmlElement(name = "groupId", required = true)
    private String groupId;

    @Getter
    @XmlElement(name = "artifactId", required = true)
    private String artifactId;

    @Getter
    @XmlElement(name = "version", required = true)
    private String version;
}
