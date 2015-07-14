package org.jboss.da.communication.aprox.model;

import lombok.Getter;
import lombok.Setter;

public class GAV extends GA {

    public GAV(String groupId, String artifactId, String version) {
        super(groupId, artifactId);
        this.version = version;
    }

    @Getter
    @Setter
    private String version;

}
