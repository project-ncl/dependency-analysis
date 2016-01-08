package org.jboss.da.communication.pnc.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

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
