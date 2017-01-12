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

        checkRequiredFields(config, "", "", "", "", "", "");
    }

    @Test
    public void userCanSetConfigurationUsingSystemProperty() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "testConfig.json");

        DAConfig config = configuration.getConfig();

        checkRequiredFields(config, "pnc-server", "aprox-server", "aprox-group",
                "aprox-group-public", "backup-scm-url", "cartographer-server-url");
        assertEquals(100000, config.getAproxRequestTimeout().intValue());
    }

    @Test
    public void configurationWithoutPropsWithDefaultValues() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "configWithoutDefaults.json");

        DAConfig config = configuration.getConfig();

        checkRequiredFields(config, "pnc-server", "aprox-server", "aprox-group",
                "aprox-group-public", "backup-scm-url", "cartographer-server-url");
        assertEquals(600000, config.getAproxRequestTimeout().intValue());
    }

    private void checkRequiredFields(DAConfig config, String pncServer, String aproxServer,
            String aproxGroup, String aproxGroupPublic, String backupScmUrl,
            String cartographerServerUrl) {
        assertEquals(pncServer, config.getPncServer());
        assertEquals(aproxServer, config.getAproxServer());
        assertEquals(aproxGroup, config.getAproxGroup());
        assertEquals(aproxGroupPublic, config.getAproxGroupPublic());
        assertEquals(backupScmUrl, config.getBackupScmUrl());
        assertEquals(cartographerServerUrl, config.getCartographerServerUrl());
    }
}
