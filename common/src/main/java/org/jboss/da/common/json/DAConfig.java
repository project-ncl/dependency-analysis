package org.jboss.da.common.json;

import lombok.Getter;
import lombok.Setter;

public class DAConfig extends AbstractModuleConfig {

    @Getter
    @Setter
    private String keycloakServer;

    @Getter
    @Setter
    private String keycloakClientid;

    @Getter
    @Setter
    private String keycloakUsername;

    @Getter
    @Setter
    private String keycloakPassword;

    @Getter
    @Setter
    private String pncServer;

    @Getter
    @Setter
    private String keycloakRealm;

    @Getter
    @Setter
    private String aproxServer;

    @Getter
    @Setter
    private String aproxGroup;

    @Getter
    @Setter
    private String backupScmUrl;

    @Getter
    @Setter
    private String backupScmBranch;

    @Getter
    @Setter
    private String repourUrl;

}
