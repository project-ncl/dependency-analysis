package org.jboss.da.communication.indy.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.ToString;

/**
 * This entity is deserialized from XML file
 * 
 * @author sknot
 */
@ToString
public class Versions {

    @Getter
    @XmlElement
    private List<String> version;

}
