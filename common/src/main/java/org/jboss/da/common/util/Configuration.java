package org.jboss.da.common.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.da.common.config.DaAppConfig;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.IndySection;
import org.jboss.da.common.json.LookupMode;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

@ApplicationScoped
public class Configuration {

    private final DAConfig daConfig;

    @Inject
    public Configuration(DaAppConfig appConfig) {
        this.daConfig = toDaConfig(appConfig);
    }

    public DAConfig getConfig() {
        return daConfig;
    }

    private static DAConfig toDaConfig(DaAppConfig appConfig) {
        DAConfig d = new DAConfig();
        d.setPncUrl(appConfig.pncUrl());

        IndySection indy = new IndySection();
        DaAppConfig.Indy appIndy = appConfig.indy();
        indy.setIndyUrl(appIndy.indyUrl());
        indy.setIndyGroup(appIndy.indyGroup());
        indy.setIndyGroupPublic(appIndy.indyGroupPublic());
        indy.setIndyRequestTimeout(appIndy.indyRequestTimeout());
        indy.setIndyRequestRetries(appIndy.indyRequestRetries());
        d.setIndy(indy);

        d.setModes(toLookupModes(appConfig.lookupModes()));
        return d;
    }

    private static List<LookupMode> toLookupModes(List<DaAppConfig.LookupMode> sections) {
        List<LookupMode> modes = new ArrayList<>();
        for (DaAppConfig.LookupMode s : sections) {
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
