package org.jboss.da.common.config;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "da")
public interface Configuration {

    @NotBlank
    @WithName("pnc-url")
    String pncUrl();

    @WithDefault("INDY") // keep Indy as default for backward compatibility
    RepositoryManager repositoryManagerSupport();

    Indy indy();

    Artifactory artifactory();

    enum RepositoryManager {
        INDY, ARTIFACTORY, DISABLED
    }

    interface Indy {
        @NotBlank
        @WithName("indy-url")
        String indyUrl();

        @NotBlank
        @WithName("indy-group")
        String indyGroup();

        @NotBlank
        @WithName("indy-group-public")
        String indyGroupPublic();

        @WithName("indy-request-timeout")
        @WithDefault("600000")
        int indyRequestTimeout();

        @WithName("indy-request-retries")
        @WithDefault("10")
        int indyRequestRetries();
    }

    interface Artifactory {

        Optional<URI> url();

        Optional<String> accessToken();

        Map<RepositoryType, @NotBlank String> groups();

        @WithDefault("PT3M")
        Duration requestTimeout();

        @WithDefault("10")
        int requestRetries();
    }

    List<LookupMode> lookupModes();

    interface LookupMode {
        String name();

        List<String> suffixes();

        @WithName("increment-suffix")
        @WithDefault("")
        String incrementSuffix();

        @WithName("build-categories")
        List<BuildCategory> buildCategories();

        @WithName("artifact-qualities")
        List<ArtifactQuality> artifactQualities();
    }
}
