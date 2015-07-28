package org.jboss.da.common.json;

import lombok.Getter;

public class DAConfig {

    @Getter
    private String keycloakServer;

    @Getter
    private String keycloakClientid;

    @Getter
    private String keycloakUsername;

    @Getter
    private String keycloakPassword;

    @Getter
    private String pncServer;

    @Getter
    private String keycloakRealm;

    @Getter
    private String aproxServer;

    @Getter
    private String aproxGroup;

}
