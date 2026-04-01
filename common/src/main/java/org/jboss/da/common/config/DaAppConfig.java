package org.jboss.da.common.config;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "da")
public interface DaAppConfig {

    @NotBlank
    @WithName("pnc-url")
    String pncUrl();

    Indy indy();

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
