package org.jboss.da.communication.pnc.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductMilestone {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private Date endDate;

    @Getter
    @Setter
    private Date startingDate;

    @Getter
    @Setter
    private Date plannedEndDate;

    @Getter
    @Setter
    private String downloadUrl;

    @Getter
    @Setter
    private int productVersionId;

    @Getter
    @Setter
    private int performedBuildRecordSetId;

    @Getter
    @Setter
    private int distributedBuildRecordSetId;

    @Getter
    @Setter
    private int productReleaseId;
}
