package org.jboss.da.rest.listings.api.model;

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
@XmlRootElement(name = "success")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SuccessResponse {

    @Getter
    @Setter
    @XmlElement(required = true, name = "success")
    protected boolean success;

}
