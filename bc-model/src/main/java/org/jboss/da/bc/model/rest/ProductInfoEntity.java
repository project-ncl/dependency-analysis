package org.jboss.da.bc.model.rest;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class ProductInfoEntity extends InfoEntity {

    @Getter
    @Setter
    protected String productVersion;

}
