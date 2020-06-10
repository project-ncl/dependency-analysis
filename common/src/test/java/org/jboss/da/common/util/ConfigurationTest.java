package org.jboss.da.common.util;

import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.da.common.util.Configuration.CONFIG_SYSPROP;
import static org.junit.Assert.*;

public class ConfigurationTest {

    private String backupConfigPath;

    private Configuration configuration;

    @Before
    public void before() {
        configuration = new Configuration();
        backupSystemConfigPath();
    }

    private void backupSystemConfigPath() {
        backupConfigPath = System.getProperty(CONFIG_SYSPROP);
    }

    @After
    public void restoreConfigPath() {
        if (backupConfigPath != null) {
            System.setProperty(CONFIG_SYSPROP, backupConfigPath);
        } else {
            System.getProperties().remove(CONFIG_SYSPROP);
        }
    }

    @Test
    public void defaultConfigurationIsLoaded() throws ConfigurationParseException {
        System.getProperties().remove(CONFIG_SYSPROP);

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(globalConfig, config, "", "", "", "");
    }

    @Test
    public void userCanSetConfigurationUsingSystemProperty() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "testConfig.json");

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(
                globalConfig,
                config,
                "http://127.0.0.1:8004",
                "http://127.0.0.1:8002",
                "indy-da-group",
                "indy-da-group-public");
        assertEquals(100000, config.getIndyRequestTimeout().intValue());
    }

    @Test
    public void configurationWithoutPropsWithDefaultValues() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "configWithoutDefaults.json");

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(
                globalConfig,
                config,
                "http://127.0.0.1:8004",
                "http://127.0.0.1:8002",
                "indy-da-group",
                "indy-da-group-public");
        assertEquals(600000, config.getIndyRequestTimeout().intValue());
    }

    private void checkRequiredFields(
            GlobalConfig globalConfig,
            DAConfig config,
            String indyServer,
            String cartographerServerUrl,
            String indyGroup,
            String indyGroupPublic) {
        assertEquals(indyServer, globalConfig.getIndyUrl());
        assertEquals(cartographerServerUrl, globalConfig.getCartographerUrl());
        assertEquals(indyGroup, config.getIndyGroup());
        assertEquals(indyGroupPublic, config.getIndyGroupPublic());
    }
}
