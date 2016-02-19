package org.jboss.da.common.json;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.List;
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
