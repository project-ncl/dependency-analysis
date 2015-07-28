package org.jboss.da.common.util;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.da.common.json.DAConfig;

public class Configuration {
    private static final String CONFIG_SYSPROP = "da-config-file";

    public DAConfig getConfig() throws ConfigurationParseException {

        try(InputStream configStream = getConfigStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(configStream, DAConfig.class);
        } catch (Exception e) {
            throw new ConfigurationParseException(e.getMessage());
        }
    }

    private InputStream getConfigStream() throws IOException {
        String configFileName = System.getProperty(CONFIG_SYSPROP);

        if (configFileName == null) {
            configFileName = "da-config.json";
        }

        //Try to open stream from full path
        File file = new File(configFileName);
        if (file.exists()) {
            return new FileInputStream(file);
        }

        //Try to open stream using classloader
        final InputStream inStream = getClass().getClassLoader().getResourceAsStream(configFileName);
        if (inStream != null) {
            return inStream;
        }

        throw new FileNotFoundException("Missing project config file.");
    }
}
