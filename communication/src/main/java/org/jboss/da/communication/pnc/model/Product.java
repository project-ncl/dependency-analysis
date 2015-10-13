package org.jboss.da.communication.pnc.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
public class Product {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    @NonNull
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String abbreviation;

    @Getter
    @Setter
    private String productCode;

    @Getter
    @Setter
    private String pgmSystemName;

    @Getter
    @Setter
    private List<Integer> productVersionIds;

}
