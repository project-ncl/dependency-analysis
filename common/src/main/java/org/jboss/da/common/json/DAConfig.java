package org.jboss.da.common.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DAConfig extends AbstractModuleConfig {

    @Getter
    @Setter
    private String pncServer;

    @Getter
    @Setter
    private String aproxServer;

    @Getter
    @Setter
    private String aproxGroup;

    @Getter
    @Setter
    private String aproxGroupPublic;

    @Getter
    @Setter
    private String backupScmUrl;

    @Getter
    @Setter
    private String backupScmBranch;

    @Getter
    @Setter
    private String repourUrl;

    @Getter
    @Setter
    private String cartographerServerUrl;
}
