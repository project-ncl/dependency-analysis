package org.jboss.da.communication.aprox.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@RequiredArgsConstructor
public class GA {

    @Setter
    @Getter
    @NonNull
    private String groupId;

    @Setter
    @Getter
    @NonNull
    private String artifactId;

}
