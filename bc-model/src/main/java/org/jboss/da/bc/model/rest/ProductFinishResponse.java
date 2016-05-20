package org.jboss.da.bc.model.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@XmlAccessorType(XmlAccessType.FIELD)
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
