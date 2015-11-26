package org.jboss.da.rest.listings.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

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
@JsonSerialize(include = Inclusion.NON_NULL)
public class SuccessResponse {

    @Getter
    @Setter
    @XmlElement(required = true, name = "success")
    protected boolean success;

    @Getter
    @Setter
    @XmlElement(required = false, name = "success")
    protected String message;

}
