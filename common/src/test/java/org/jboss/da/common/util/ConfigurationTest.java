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

import org.jboss.da.common.config.Configuration;
import org.jboss.da.common.lookup.LookupMode;
import org.jboss.pnc.enums.BuildCategory;
import org.junit.jupiter.api.Test;

import io.smallrye.config.ConfigValidationException;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;

public class ConfigurationTest {

    @Test
    public void defaultConfigurationIsLoaded() {
        assertThrows(ConfigValidationException.class, () -> loadFromClasspathYaml("/da-config-1.yaml"));
    }

    @Test
    public void userCanSetConfigurationUsingYamlFile() throws IOException {
        Configuration configuration = loadFromClasspathYaml("/testConfig.yaml");

        assertEquals("http://127.0.0.1:8005", configuration.pncUrl());
        assertEquals("http://127.0.0.1:8004", Urls.withoutTrailingSlash(configuration.indy().indyUrl()));
        assertEquals("indy-da-group", configuration.indy().indyGroup());
        assertEquals("indy-da-group-public", configuration.indy().indyGroupPublic());
        assertEquals(100000, configuration.indy().indyRequestTimeout());
    }

    @Test
    public void configurationWithoutPropsWithDefaultValues() throws IOException {
        Configuration configuration = loadFromClasspathYaml("/configWithoutDefaults.yaml");

        assertEquals("http://127.0.0.1:8005", configuration.pncUrl());
        assertEquals("http://127.0.0.1:8004", Urls.withoutTrailingSlash(configuration.indy().indyUrl()));
        assertEquals("indy-da-group", configuration.indy().indyGroup());
        assertEquals("indy-da-group-public", configuration.indy().indyGroupPublic());
        assertEquals(600000, configuration.indy().indyRequestTimeout());
    }

    @Test
    public void testDefaultLookupModes() throws IOException {
        Configuration configuration = loadFromClasspathYaml("/da-config-2.yaml");

        List<LookupMode> modes = configuration.lookupModes().stream().map(LookupMode::from).toList();

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

    private static Configuration loadFromClasspathYaml(String classpathResource) throws IOException {
        URL url = ConfigurationTest.class.getResource(classpathResource);
        assertNotNull(url, "Missing classpath resource: " + classpathResource);
        SmallRyeConfig smallRyeConfig = new SmallRyeConfigBuilder()
                .withSources(new YamlConfigSource(url))
                .withMapping(Configuration.class)
                .build();
        return smallRyeConfig.getConfigMapping(Configuration.class);
    }
}
