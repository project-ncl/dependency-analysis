package org.jboss.da.bc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class ProductInfoEntity extends InfoEntity {

    @Getter
    @Setter
    protected String productVersion;

}
