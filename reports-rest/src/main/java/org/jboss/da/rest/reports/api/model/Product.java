package org.jboss.da.rest.reports.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@RequiredArgsConstructor
public class Product {

    @Getter
    @NonNull
    @XmlElement(required = true, name = "name")
    private final String name;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "version")
    private final String version;
}
