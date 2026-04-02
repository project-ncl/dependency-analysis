package org.jboss.da.common.lookup;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.da.common.config.Configuration;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class LookupMode {
    private String name;
    private List<String> suffixes = new ArrayList<>();
    private String incrementSuffix;
    private EnumSet<BuildCategory> buildCategories = EnumSet.noneOf(BuildCategory.class);
    private EnumSet<ArtifactQuality> artifactQualities = EnumSet.noneOf(ArtifactQuality.class);

    public static LookupMode from(Configuration.LookupMode section) {
        EnumSet<BuildCategory> buildCategories = section.buildCategories().isEmpty()
                ? EnumSet.noneOf(BuildCategory.class)
                : EnumSet.copyOf(new ArrayList<>(section.buildCategories()));
        EnumSet<ArtifactQuality> qualities = section.artifactQualities().isEmpty()
                ? EnumSet.noneOf(ArtifactQuality.class)
                : EnumSet.copyOf(new ArrayList<>(section.artifactQualities()));
        return builder()
                .name(section.name())
                .suffixes(new ArrayList<>(section.suffixes()))
                .incrementSuffix(section.incrementSuffix())
                .buildCategories(buildCategories)
                .artifactQualities(qualities)
                .build();
    }

    /**
     * This used to be in ReportsGeneratorImpl and LookupGeneratorImpl but was normalized to a single function
     * here.
     *
     * @param appConfig the configuration to parse
     * @return the lookup modes.
     */
    public static Map<String, LookupMode> indexByName(Configuration appConfig) {
        return appConfig.lookupModes()
                .stream()
                .map(LookupMode::from)
                .collect(Collectors.toMap(LookupMode::getName, Function.identity()));
    }
}
