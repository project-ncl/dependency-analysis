package org.jboss.da.communication.pnc.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRelease {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String version;

    private Date releaseDate;

    @Getter
    @Setter
    private String downloadUrl;

    @Getter
    @Setter
    private int productVersionId;

    @Getter
    @Setter
    private int productMilestoneId;

    @Getter
    @Setter
    private String supportLevel;

    public Date getReleaseDate() {
        return releaseDate == null ? null : new Date(releaseDate.getTime());
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate == null ? null : new Date(releaseDate.getTime());
    }

}
