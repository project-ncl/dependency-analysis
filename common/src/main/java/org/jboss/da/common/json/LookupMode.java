package org.jboss.da.common.json;

import lombok.Data;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
public class LookupMode {
    private String name;
    private List<String> suffixes = new ArrayList<>();
    private BuildCategory buildCategory;
    private EnumSet<ArtifactQuality> artifactQualities = EnumSet.noneOf(ArtifactQuality.class);
}
