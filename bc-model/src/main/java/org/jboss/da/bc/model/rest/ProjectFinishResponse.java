package org.jboss.da.bc.model.rest;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ProjectFinishResponse extends FinishResponse<ProjectInfoEntity> {

    @Getter
    protected Integer bcSetId;

    @Override
    public void setCreatedEntityId(Integer id) {
        bcSetId = id;
    }

}
