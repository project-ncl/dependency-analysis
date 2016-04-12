package org.jboss.da.bc.model.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ProjectFinishResponse extends FinishResponse {

    @Getter
    protected Integer bcSetId;

    @Getter
    @Setter
    protected ProjectInfoEntity entity;

    @Override
    public void setCreatedEntityId(Integer id) {
        bcSetId = id;
    }

}
