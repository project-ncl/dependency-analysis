package org.jboss.da.communication.pnc.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@RequiredArgsConstructor
public class ProductVersion {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    @NonNull
    private String version;

    @Getter
    @Setter
    @NonNull
    private Integer productId;

}
