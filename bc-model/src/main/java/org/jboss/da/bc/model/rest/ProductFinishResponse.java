package org.jboss.da.bc.model.rest;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ProductFinishResponse extends FinishResponse<ProductInfoEntity> {

    @Getter
    protected Integer productVersionId;

    @Override
    public void setCreatedEntityId(Integer id) {
        productVersionId = id;
    }

}
