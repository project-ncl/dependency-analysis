package org.jboss.da.bc;

import org.jboss.da.bc.model.rest.InfoEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class AnalyseNextLevelExceptionContainer {

    @Setter
    @Getter
    private Exception exception;

    @Setter
    @Getter
    private InfoEntity bc;
}
