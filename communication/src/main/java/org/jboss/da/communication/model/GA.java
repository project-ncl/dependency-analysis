package org.jboss.da.communication.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@NoArgsConstructor
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

    @Override
    public String toString() {
        return groupId + ":" + artifactId;
    }

}
