package org.jboss.da.common.util;

import java.io.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.jboss.da.common.json.AbstractModuleGroup;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.DAGroupWrapper;
import org.jboss.da.common.json.ModuleConfigJson;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Configuration {

    static final String CONFIG_SYSPROP = "da-config-file";

    static final String CONFIG_DEFAULT = "da-config.json";

    public DAConfig getConfig() throws ConfigurationParseException {

        try (InputStream configStream = getConfigStream()) {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerSubtypes(new NamedType(DAGroupWrapper.class, "da"));
            mapper.registerSubtypes(new NamedType(DAConfig.class, "da-config"));
            ModuleConfigJson configs = mapper.readValue(configStream, ModuleConfigJson.class);

            for (AbstractModuleGroup c : configs.getConfigs()) {
                if (c.getClass().isAssignableFrom(DAGroupWrapper.class)) {
                    DAGroupWrapper daConfigWrapper = (DAGroupWrapper) c;
                    return daConfigWrapper.getConfiguration();
                }
            }

            throw new ConfigurationParseException("Config for Dependency Analysis not found");

        } catch (RuntimeException | IOException e) {
            throw new ConfigurationParseException(e);
        }
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
        final InputStream inStream = getClass().getClassLoader()
                .getResourceAsStream(configFileName);
        if (inStream != null) {
            return inStream;
        }

        throw new FileNotFoundException("Missing project config file " + configFileName + ".");
    }
}
