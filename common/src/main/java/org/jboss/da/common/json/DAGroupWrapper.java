package org.jboss.da.common.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

public class DAGroupWrapper extends AbstractModuleGroup {

    @Setter
    @JsonProperty("configs")
    private List<AbstractModuleConfig> configs;

    public DAConfig getConfiguration() {
        for (AbstractModuleConfig config : configs) {
            if (DAConfig.class.isAssignableFrom(config.getClass())) {
                return (DAConfig) config;
            }
        }

        // if we're here, no config found
        return null;
    }
}
