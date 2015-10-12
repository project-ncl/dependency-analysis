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
@EqualsAndHashCode
@ToString
public class FinishResponse {

    @Getter
    @Setter
    protected Boolean success;

    @Getter
    @Setter
    protected String message;

    @Getter
    @Setter
    protected InfoEntity entity;

}
