package org.jboss.da.test.client.rest.listings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestProduct {

    @Getter
    @Setter
    protected long id;

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected String status;

}
