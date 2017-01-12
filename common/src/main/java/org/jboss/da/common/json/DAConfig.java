package org.jboss.da.common.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    private String cartographerServerUrl;

    @Getter
    @Setter
    @JsonProperty(required = false)
    private Integer defaultHttpRequestTimeout = 600000;

}
