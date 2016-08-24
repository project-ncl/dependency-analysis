package org.jboss.da.listings.model.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {

    @Getter
    @Setter
    @XmlElement(required = true, name = "success")
    protected boolean success;

    @Getter
    @Setter
    protected Long id;

    @Getter
    @Setter
    @XmlElement(required = false, name = "message")
    protected String message;

}
