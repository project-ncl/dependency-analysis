package org.jboss.da.communication.pnc.model;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@AllArgsConstructor
public class ProductVersion {

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private Integer productId;

}
