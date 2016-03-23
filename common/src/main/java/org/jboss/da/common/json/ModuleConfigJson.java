package org.jboss.da.common.json;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ModuleConfigJson {

    public String name;

    public List<AbstractModuleGroup> configs;

    @JsonCreator
    public ModuleConfigJson(@JsonProperty("name") String name) {
        this.name = name;
        configs = new ArrayList<AbstractModuleGroup>();
    }

    public void setConfigs(List<AbstractModuleGroup> configs) {
        this.configs = configs;
    }

    public void addConfig(AbstractModuleGroup moduleConfig) {
        configs.add(moduleConfig);
    }

    public List<AbstractModuleGroup> getConfigs() {
        return configs;
    }

    @Override
    public String toString() {
        return "ModuleConfigJson [name=" + name + ", configs=" + configs + "]";
    }
}
