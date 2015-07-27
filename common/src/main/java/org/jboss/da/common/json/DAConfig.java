package org.jboss.da.common.json;

public class DAConfig {

    private String keycloakServer;

    private String keycloakClientid;

    private String keycloakUsername;

    private String keycloakPassword;

    private String pncServer;

    private String keycloakRealm;

    private String aproxServer;

    public String getKeycloakServer() {
        return keycloakServer;
    }

    public String getKeycloakClientid() {
        return keycloakClientid;
    }

    public String getKeycloakUsername() {
        return keycloakUsername;
    }

    public String getKeycloakPassword() {
        return keycloakPassword;
    }

    public String getPncServer() {
        return pncServer;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public String getAproxServer() {
        return aproxServer;
    }

    public void setAproxServer(String aproxServer) {
        this.aproxServer = aproxServer;
    }
}
