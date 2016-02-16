package org.jboss.da.communication.pnc.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

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

    @Getter
    @Setter
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

}
