package org.jboss.da.common.json;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DAConfig extends AbstractModuleConfig {

    private String indyGroup;

    private String indyGroupPublic;

    @JsonProperty(required = false)
    private Integer indyRequestTimeout = 600000;

    private List<LookupMode> modes;

}
