package org.jboss.da.communication.pnc.model;

import java.util.List;

public class Project {
    private int id;
    private String name;
    private String description;
    private String issueTrackerUrl;
    private String projectUrl;
    private List<Integer> configurationIds;
    private String licenseId;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public List<Integer> getConfigurationIds() {
        return configurationIds;
    }

    public void setConfigurationIds(List<Integer> configurationIds) {
        this.configurationIds = configurationIds;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }
}
