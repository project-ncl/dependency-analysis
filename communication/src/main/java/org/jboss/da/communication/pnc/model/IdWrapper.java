package org.jboss.da.communication.pnc.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdWrapper {

    @Getter
    @Setter
    private Integer id;
}
