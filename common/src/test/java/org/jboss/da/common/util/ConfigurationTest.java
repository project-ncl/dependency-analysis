package org.jboss.da.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.da.common.config.DaAppConfig;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.pnc.enums.BuildCategory;
import org.junit.jupiter.api.Test;

import io.smallrye.config.ConfigValidationException;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;
import io.smallrye.config.validator.BeanValidationConfigValidatorImpl;

public class ConfigurationTest {

    @Test
    public void defaultConfigurationIsLoaded() {
        assertThrows(ConfigValidationException.class, () -> configurationFromClasspathYaml("/da-config-1.yaml"));
    }

    @Test
    public void userCanSetConfigurationUsingYamlFile() throws IOException {
        Configuration configuration = configurationFromClasspathYaml("/testConfig.yaml");

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(globalConfig, config, "http://127.0.0.1:8004", "indy-da-group", "indy-da-group-public");
        assertEquals(100000, config.getIndyRequestTimeout().intValue());
    }

    @Test
    public void configurationWithoutPropsWithDefaultValues() throws IOException {
        Configuration configuration = configurationFromClasspathYaml("/configWithoutDefaults.yaml");

        GlobalConfig globalConfig = configuration.getGlobalConfig();
        DAConfig config = configuration.getConfig();

        checkRequiredFields(globalConfig, config, "http://127.0.0.1:8004", "indy-da-group", "indy-da-group-public");
        assertEquals(600000, config.getIndyRequestTimeout().intValue());
    }

    @Test
    public void testDefaultLookupModes() throws IOException {
        Configuration configuration = configurationFromClasspathYaml("/da-config-2.yaml");

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
        assertEquals("redhat", persistent.getIncrementSuffix());
        assertEquals(1, persistent.getBuildCategories().size());
        assertTrue(persistent.getBuildCategories().contains(BuildCategory.STANDARD));
        assertFalse(persistent.getArtifactQualities().isEmpty());
    }

    private static Configuration configurationFromClasspathYaml(String classpathResource) throws IOException {
        URL url = ConfigurationTest.class.getResource(classpathResource);
        assertNotNull(url, "Missing classpath resource: " + classpathResource);
        SmallRyeConfig smallRyeConfig = new SmallRyeConfigBuilder()
                .withSources(new YamlConfigSource(url))
                .withValidateUnknown(false)
                .withValidator(new BeanValidationConfigValidatorImpl())
                .withMapping(DaAppConfig.class)
                .build();
        DaAppConfig appConfig = smallRyeConfig.getConfigMapping(DaAppConfig.class);
        return new Configuration(appConfig);
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
