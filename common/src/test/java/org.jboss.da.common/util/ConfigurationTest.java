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

        assertEquals("", config.getKeycloakServer());
        assertEquals("", config.getKeycloakRealm());
        assertEquals("", config.getKeycloakClientid());
        assertEquals("", config.getKeycloakUsername());
        assertEquals("", config.getKeycloakPassword());
        assertEquals("", config.getPncServer());

    }

    @Test
    public void userCanSetConfigurationUsingSystemProperty() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "testConfig.json");

        DAConfig config = configuration.getConfig();

        assertEquals("keycloak-server", config.getKeycloakServer());
        assertEquals("keycloak-realm", config.getKeycloakRealm());
        assertEquals("keycloak-client-id", config.getKeycloakClientid());
        assertEquals("keycloak-username", config.getKeycloakUsername());
        assertEquals("keycloak-password", config.getKeycloakPassword());
        assertEquals("pnc-server", config.getPncServer());

    }
}