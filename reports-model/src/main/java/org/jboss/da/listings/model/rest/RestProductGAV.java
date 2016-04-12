package org.jboss.da.listings.model.rest;

import org.jboss.da.listings.model.ProductSupportStatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RestProductGAV {

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected ProductSupportStatus supportStatus;

    @Getter
    @Setter
    protected RestArtifact gav;
}
