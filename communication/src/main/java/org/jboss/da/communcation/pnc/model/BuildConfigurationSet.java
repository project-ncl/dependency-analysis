package org.jboss.da.communcation.pnc.model;

import java.util.List;

public class BuildConfigurationSet {
    private int id;
    private String name;
    private int productVersionId;
    private List<Integer> buildConfigurationIds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(int productVersionId) {
        this.productVersionId = productVersionId;
    }

    public List<Integer> getBuildConfigurationIds() {
        return buildConfigurationIds;
    }

    public void setBuildConfigurationIds(List<Integer> buildConfigurationIds) {
        this.buildConfigurationIds = buildConfigurationIds;
    }
}
