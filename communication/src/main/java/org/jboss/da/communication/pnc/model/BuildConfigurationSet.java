package org.jboss.da.communication.pnc.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationSet {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private int productVersionId;

    @Getter
    @Setter
    private List<Integer> buildConfigurationIds;

}
