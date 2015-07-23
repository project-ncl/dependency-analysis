package org.jboss.da.communication.aprox.model;


import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
        return "GA [groupId=" + groupId + ", artifactId=" + artifactId + "]";
    }
    
    

}
