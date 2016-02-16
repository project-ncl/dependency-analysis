package org.jboss.da.communication.pnc.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

@RequiredArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVersion {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    @NonNull
    private String version;

    @Getter
    @Setter
    @NonNull
    private Integer productId;

    @Getter
    @Setter
    private Integer currentProductMilestoneId;

    @Getter
    @Setter
    @NonNull
    private List<BuildConfigurationSet> buildConfigurationSets = new ArrayList<>();

    @Getter
    @Setter
    @NonNull
    private List<BuildConfiguration> buildConfigurations = new ArrayList<>();

    @Getter
    @Setter
    @NonNull
    private List<ProductMilestone> productMilestones = new ArrayList<>();

    @Getter
    @Setter
    @NonNull
    private List<Integer> productReleases = new ArrayList<>();

}
