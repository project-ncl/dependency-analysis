package org.jboss.da.communication.aprox.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Versions {

    @Getter
    @XmlElement
    private List<String> version;

}
