package org.jboss.da.common.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.da.common.config.DaAppConfig;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

@ApplicationScoped
public class Configuration {

    private final DAConfig daConfig;

    private final GlobalConfig globalConfig;

    @Inject
    public Configuration(DaAppConfig appConfig) {
        this.globalConfig = toGlobalConfig(appConfig);
        this.daConfig = toDaConfig(appConfig);
    }

    public DAConfig getConfig() {
        return daConfig;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    private static GlobalConfig toGlobalConfig(DaAppConfig appConfig) {
        GlobalConfig g = new GlobalConfig();
        g.setIndyUrl(appConfig.indy().indyUrl());
        g.setPncUrl(appConfig.pncUrl());
        return g;
    }

    private static DAConfig toDaConfig(DaAppConfig appConfig) {
        DaAppConfig.IndySection indy = appConfig.indy();
        DAConfig d = new DAConfig();
        d.setIndyGroup(indy.indyGroup());
        d.setIndyGroupPublic(indy.indyGroupPublic());
        d.setIndyRequestTimeout(indy.indyRequestTimeout());
        d.setIndyRequestRetries(indy.indyRequestRetries());
        d.setModes(toLookupModes(appConfig.lookupModes()));
        return d;
    }

    private static List<LookupMode> toLookupModes(List<DaAppConfig.LookupModeSection> sections) {
        List<LookupMode> modes = new ArrayList<>();
        for (DaAppConfig.LookupModeSection s : sections) {
            EnumSet<BuildCategory> buildCategories = s.buildCategories().isEmpty()
                    ? EnumSet.noneOf(BuildCategory.class)
                    : EnumSet.copyOf(new ArrayList<>(s.buildCategories()));
            EnumSet<ArtifactQuality> qualities = s.artifactQualities().isEmpty()
                    ? EnumSet.noneOf(ArtifactQuality.class)
                    : EnumSet.copyOf(new ArrayList<>(s.artifactQualities()));
            modes.add(
                    LookupMode.builder()
                            .name(s.name())
                            .suffixes(new ArrayList<>(s.suffixes()))
                            .incrementSuffix(s.incrementSuffix())
                            .buildCategories(buildCategories)
                            .artifactQualities(qualities)
                            .build());
        }
        return modes;
    }
}
