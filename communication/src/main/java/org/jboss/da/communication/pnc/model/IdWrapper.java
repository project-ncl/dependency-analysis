package org.jboss.da.communication.pnc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdWrapper {

    @Getter
    @Setter
    private Integer id;
}
