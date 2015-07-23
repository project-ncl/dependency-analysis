package org.jboss.da.communication.aprox.model;

import javax.xml.bind.annotation.XmlElement;

public class Versioning {

    private String latestVersion;

    private String latestRelease;

    private Versions versions;

    public String getLatestVersion() {
        return latestVersion;
    }

    @XmlElement(name = "latest")
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestRelease() {
        return latestRelease;
    }

    @XmlElement(name = "release")
    public void setLatestRelease(String latestRelease) {
        this.latestRelease = latestRelease;
    }

    public Versions getVersions() {
        return versions;
    }

    @XmlElement
    public void setVersions(Versions versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return "Versioning [latestVersion=" + latestVersion + ", latestRelease="
                + latestRelease + "]" + versions;
    }
}
