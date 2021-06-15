package org.jboss.da.common.json;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
}
