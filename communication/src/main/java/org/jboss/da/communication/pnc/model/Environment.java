package org.jboss.da.communication.pnc.model;

import lombok.Getter;
import lombok.Setter;

public class Environment {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String imageRepositoryUrl;

    @Getter
    @Setter
    private String buildType;

    @Getter
    @Setter
    private String imageId;
}
