package org.jboss.da.listings.rest.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@XmlRootElement(name = "artifact")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RestArtifact {

    @Getter
    @Setter
    @XmlElement(required = true, name = "group_id")
    protected String groupId;

    @Getter
    @Setter
    @XmlElement(required = true, name = "artifact_id")
    protected String artifactId;

    @Getter
    @Setter
    @XmlElement(required = true, name = "version")
    protected String version;

}
