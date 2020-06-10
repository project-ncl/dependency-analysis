package org.jboss.da.common.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DAConfig extends AbstractModuleConfig {

    @Getter
    @Setter
    private String indyGroup;

    @Getter
    @Setter
    private String indyGroupPublic;

    @Getter
    @Setter
    @JsonProperty(required = false)
    private Integer indyRequestTimeout = 600000;

}
