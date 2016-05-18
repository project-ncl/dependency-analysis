package org.jboss.da.common.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
        property = "@module-config")
public class AbstractModuleConfig {
}
