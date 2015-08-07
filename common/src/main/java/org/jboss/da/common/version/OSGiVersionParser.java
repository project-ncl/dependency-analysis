package org.jboss.da.common.version;

import org.commonjava.maven.ext.manip.impl.Version;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OSGiVersionParser {

    public String getOSGiVersion(String version) {
        Version osgi = new Version(version);
        return osgi.getOSGiVersionString();
    }

}
