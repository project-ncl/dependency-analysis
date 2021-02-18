package org.jboss.da.common.json;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import lombok.Data;

@Data
public class LookupMode {
    private String name;
    private List<String> suffixes = new ArrayList<>();
    private BuildCategory buildCategory;
    private EnumSet<ArtifactQuality> artifactQualities = EnumSet.noneOf(ArtifactQuality.class);
}
