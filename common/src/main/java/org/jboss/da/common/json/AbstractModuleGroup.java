package org.jboss.da.common.json;

import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
        property = "@module-group", defaultImpl = DefaultModuleGroup.class)
public class AbstractModuleGroup {
}
