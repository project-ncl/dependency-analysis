package org.jboss.da.communication.aprox.model;

import org.jboss.da.communication.model.GAV;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class GAVDependencyTree {

    @Getter
    @Setter
    @NonNull
    private GAV gav;

    @Getter
    private Set<GAVDependencyTree> dependencies = new HashSet<>();

    public void addDependency(GAVDependencyTree dep) {
        dependencies.add(dep);
    }

}
