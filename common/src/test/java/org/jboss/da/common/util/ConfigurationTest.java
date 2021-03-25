package org.jboss.da.common.util;

import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.pnc.enums.BuildCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.da.common.util.Configuration.CONFIG_SYSPROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

        checkRequiredFields(globalConfig, config, "", "", "");
    }

    @Test
    public void userCanSetConfigurationUsingSystemProperty() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "testConfig.json");

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(globalConfig, config, "http://127.0.0.1:8004", "indy-da-group", "indy-da-group-public");
        assertEquals(100000, config.getIndyRequestTimeout().intValue());
    }

    @Test
    public void configurationWithoutPropsWithDefaultValues() throws ConfigurationParseException {
        System.setProperty(CONFIG_SYSPROP, "configWithoutDefaults.json");

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(globalConfig, config, "http://127.0.0.1:8004", "indy-da-group", "indy-da-group-public");
        assertEquals(600000, config.getIndyRequestTimeout().intValue());
    }

    @Test
    public void testDefaultLookupModes() throws ConfigurationParseException {
        System.getProperties().remove(CONFIG_SYSPROP);

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        List<LookupMode> modes = config.getModes();
        assertNotNull(modes);
        assertEquals(2, modes.size());
        Map<String, LookupMode> modeMap = modes.stream()
                .collect(Collectors.toMap(LookupMode::getName, Function.identity()));
        assertTrue(modeMap.containsKey("PERSISTENT"));
        assertTrue(modeMap.containsKey("TEMPORARY"));
        LookupMode persistent = modeMap.get("PERSISTENT");
        assertNotNull(persistent.getSuffixes());
        assertEquals(1, persistent.getSuffixes().size());
        assertEquals("redhat", persistent.getSuffixes().get(0));
        assertEquals(BuildCategory.STANDARD, persistent.getBuildCategory());
        assertFalse(persistent.getArtifactQualities().isEmpty());
    }

    private void checkRequiredFields(
            GlobalConfig globalConfig,
            DAConfig config,
            String indyServer,
            String indyGroup,
            String indyGroupPublic) {
        assertEquals(indyServer, globalConfig.getIndyUrl());
        assertEquals(indyGroup, config.getIndyGroup());
        assertEquals(indyGroupPublic, config.getIndyGroupPublic());
    }
}
