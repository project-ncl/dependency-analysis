package org.jboss.da.scm.api;

/**
 * Enum to represent the SCM types supported by SCM. If a new type is added here, please also provide a provide in the
 * SCM class.
 */
public enum SCMType {
    GIT("GIT"), SVN("SVN");

    private final String provider;

    private SCMType(String provider) {
        this.provider = provider;
    }

    /**
     * Returns provided url in Maven SCM format.
     * 
     * @param url
     * @return
     */
    public String getSCMUrl(String url) {
        return String.format("scm:%s:%s", provider, url);
    }
}
