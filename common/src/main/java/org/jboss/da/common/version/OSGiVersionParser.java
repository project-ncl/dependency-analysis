package org.jboss.da.common.version;

import org.commonjava.maven.ext.manip.impl.Version;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OSGiVersionParser {

    /**
     * Converts version to osgi compliant
     * 
     * @param version
     * @return
     */
    
    public String getOSGiVersion(String version) {
        Version osgi;
        if (version.matches(".*[.-]redhat-(\\d+)")) {
            osgi = new Version(version);
            return osgi.getOSGiVersionString();
        }
        osgi = new Version(version + ".redhat");
        String osgiS = osgi.getOSGiVersionString();
        if (osgiS.endsWith(".redhat"))
            return osgiS.replace(".redhat", "");
        return osgiS.replace("-redhat", "");
    }

}
