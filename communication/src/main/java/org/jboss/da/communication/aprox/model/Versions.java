package org.jboss.da.communication.aprox.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Versions {

    private List<String> version;

    public List<String> getVersion() {
        return version;
    }

    @XmlElement
    public void setVersion(List<String> version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Versions [version=" + version + "]";
    }

}
