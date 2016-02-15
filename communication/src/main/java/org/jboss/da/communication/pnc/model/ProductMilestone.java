package org.jboss.da.communication.pnc.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

public class ProductMilestone {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String version;

    private Date endDate;

    private Date startingDate;

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

    public Date getEndDate() {
        return endDate == null ? null : new Date(endDate.getTime());
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate == null ? null : new Date(endDate.getTime());
    }

    public Date getStartingDate() {
        return startingDate == null ? null : new Date(startingDate.getTime());
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate == null ? null : new Date(startingDate.getTime());
    }

    public Date getPlannedEndDate() {
        return plannedEndDate == null ? null : new Date(plannedEndDate.getTime());
    }

    public void setPlannedEndDate(Date plannedEndDate) {
        this.plannedEndDate = plannedEndDate == null ? null : new Date(plannedEndDate.getTime());
    }

}
