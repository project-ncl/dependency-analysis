package org.jboss.da.common.util;

import org.jboss.da.common.json.DAConfig;
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

        DAConfig config = configuration.getConfig();

        assertEquals("", config.getPncServer());
        assertEquals("", config.getAproxServer());
        assertEquals("", config.getAproxGroup());
        assertEquals("", config.getAproxGroupPublic());
        assertEquals("", config.getBackupScmUrl());
        assertEquals("", config.getCartographerServerUrl());
    }

    @Test
    public void userCanSetConfigurationUsingSystemProperty() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "testConfig.json");

        DAConfig config = configuration.getConfig();

        assertEquals("pnc-server", config.getPncServer());
        assertEquals("aprox-server", config.getAproxServer());
        assertEquals("aprox-group", config.getAproxGroup());
        assertEquals("aprox-group-public", config.getAproxGroupPublic());
        assertEquals("backup-scm-url", config.getBackupScmUrl());
        assertEquals("cartographer-server-url", config.getCartographerServerUrl());
    }
}
