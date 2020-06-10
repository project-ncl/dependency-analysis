package org.jboss.da.common.util;

import java.io.*;

import org.jboss.da.common.json.AbstractModuleGroup;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.DAGroupWrapper;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.json.ModuleConfigJson;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

@ApplicationScoped
public class Configuration {

    static final String CONFIG_SYSPROP = "da-config-file";

    static final String CONFIG_DEFAULT = "da-config.json";

    private ModuleConfigJson configJson;

    public DAConfig getConfig() throws ConfigurationParseException {
        ModuleConfigJson configJson = getConfigJson();

        for (AbstractModuleGroup c : configJson.getConfigs()) {
            if (c.getClass().isAssignableFrom(DAGroupWrapper.class)) {
                DAGroupWrapper daConfigWrapper = (DAGroupWrapper) c;
                return daConfigWrapper.getConfiguration();
            }
        }

        throw new ConfigurationParseException("Config for Dependency Analysis not found");
    }

    public GlobalConfig getGlobalConfig() throws ConfigurationParseException {
        ModuleConfigJson configJson = getConfigJson();

        for (AbstractModuleGroup c : configJson.getConfigs()) {
            if (c.getClass().isAssignableFrom(GlobalConfig.class)) {
                return (GlobalConfig) c;
            }
        }

        throw new ConfigurationParseException("Config for Dependency Analysis not found");
    }

    private ModuleConfigJson getConfigJson() throws ConfigurationParseException {
        if (configJson == null) {
            try (InputStream configStream = getConfigStream()) {
                ObjectMapper mapper = new ObjectMapper();

                mapper.registerSubtypes(new NamedType(GlobalConfig.class, "global"));
                mapper.registerSubtypes(new NamedType(DAGroupWrapper.class, "da"));
                mapper.registerSubtypes(new NamedType(DAConfig.class, "da-config"));
                configJson = mapper.readValue(configStream, ModuleConfigJson.class);
            } catch (RuntimeException | IOException e) {
                throw new ConfigurationParseException("Failed to read configuration", e);
            }
        }
        return configJson;
    }

    private InputStream getConfigStream() throws IOException {
        String configFileName = System.getProperty(CONFIG_SYSPROP);

        if (configFileName == null) {
            configFileName = CONFIG_DEFAULT;
        }

        // Try to open stream from full path
        File file = new File(configFileName);
        if (file.exists()) {
            return new FileInputStream(file);
        }

        // Try to open stream using classloader
        final InputStream inStream = getClass().getClassLoader().getResourceAsStream(configFileName);
        if (inStream != null) {
            return inStream;
        }

        throw new FileNotFoundException("Missing project config file " + configFileName + ".");
    }
}
