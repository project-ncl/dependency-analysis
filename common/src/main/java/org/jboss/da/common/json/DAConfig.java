package org.jboss.da.common.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DAConfig extends AbstractModuleConfig {

    private String indyGroup;

    private String indyGroupPublic;

    @JsonProperty(required = false)
    private Integer indyRequestTimeout = 600000;

    private List<LookupMode> modes;

}
