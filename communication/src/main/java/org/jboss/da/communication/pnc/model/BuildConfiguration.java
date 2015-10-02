package org.jboss.da.communication.pnc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
public class BuildConfiguration extends BuildConfigurationCreate {

    @Getter
    @Setter
    private int id;
}
