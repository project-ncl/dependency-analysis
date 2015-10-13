package org.jboss.da.communication.pnc.model;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

@RequiredArgsConstructor
@NoArgsConstructor
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
    private List<Integer> buildConfigurationSetIds = new ArrayList<>();

    @Getter
    @Setter
    @NonNull
    private List<Integer> buildConfigurationIds = new ArrayList<>();

    @Getter
    @Setter
    @NonNull
    private List<Integer> productMilestones = new ArrayList<>();

    @Getter
    @Setter
    @NonNull
    private List<Integer> productReleases = new ArrayList<>();

}
