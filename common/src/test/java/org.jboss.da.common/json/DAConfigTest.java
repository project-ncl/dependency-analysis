package org.jboss.da.common.json;

import org.jboss.da.common.util.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class DAConfigTest {

    public static String backupConfigPath;

    @BeforeClass
    public static void setUpTestConfigPath() {
        backupConfigPath = System.getProperty("da-config-file");
        System.setProperty("da-config-file", "testConfig.json");
    }

    @AfterClass
    public static void restoreConfigPath() {
        if (backupConfigPath != null) {
            System.setProperty("pnc-config-file", backupConfigPath);
        } else {
            System.getProperties().remove("pnc-config-file");
        }
    }

    @Test
    public void loadConfigProperly() {
        Configuration config = new Configuration();
        DAConfig depAnalysisConfig = config.getConfig();
        assertEquals("keycloak-server", depAnalysisConfig.getKeycloakServer());
        assertEquals("keycloak-realm", depAnalysisConfig.getKeycloakRealm());
        assertEquals("keycloak-client-id", depAnalysisConfig.getKeycloakClientid());
        assertEquals("keycloak-username", depAnalysisConfig.getKeycloakUsername());
        assertEquals("keycloak-password", depAnalysisConfig.getKeycloakPassword());
        assertEquals("pnc-server", depAnalysisConfig.getPncServer());

    }
}