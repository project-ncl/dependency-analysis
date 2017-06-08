package org.jboss.da.common.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ModuleConfigJson {

    public String name;

    public List<AbstractModuleGroup> configs;

    @JsonCreator
    public ModuleConfigJson(@JsonProperty("name") String name) {
        this.name = name;
        configs = new ArrayList<>();
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
