package org.jboss.da.bc.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class BuildConfigurationSet {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer productVersionId;

    @Getter
    @Setter
    private List<Integer> buildConfigurationIds;
}
