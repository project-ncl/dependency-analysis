package org.jboss.da.communication.aprox.model;

import org.jboss.da.model.rest.GAV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Prune duplicate subtrees leaving one instance for each duplicate.
     */
    public void prune(){
        Map<GAV, Set<GAVDependencyTree>> forest = new HashMap<>();
        fillForest(this, forest);
        Map<GAV, Set<GAVDependencyTree>> candidates = forest.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
              
        int previous;
        do{
            previous = candidates.size();
            pruneLeaves(this, candidates);
        }while(!candidates.isEmpty() && previous > candidates.size());
    }

    private static void fillForest(GAVDependencyTree tree, Map<GAV, Set<GAVDependencyTree>> forest) {
        Set<GAVDependencyTree> trees = forest.computeIfAbsent(tree.getGav(), (k) -> new HashSet<>());
        trees.add(tree);
        for(GAVDependencyTree t : tree.getDependencies()){
            fillForest(t, forest);
        }
    }

    private static void pruneLeaves(GAVDependencyTree tree,
            Map<GAV, Set<GAVDependencyTree>> candidates) {
        // Sort the dependencies by GAV so the prunnig will be deterministic
        List<GAVDependencyTree> deps = new ArrayList<>(tree.getDependencies());
        Collections.sort(deps, (a,b) -> a.gav.toString().compareTo(b.gav.toString()));

        for (GAVDependencyTree d : deps) {
            pruneLeaves(d, candidates);
        }
        Iterator<GAVDependencyTree> it = tree.getDependencies().iterator();
        while (it.hasNext()) {
            GAVDependencyTree d = it.next();
            if (d.getDependencies().isEmpty() && candidates.containsKey(d.getGav())) {
                it.remove();
                Set<GAVDependencyTree> forest = candidates.get(d.getGav());
                forest.remove(d);
                if (forest.size() <= 1) {
                    candidates.remove(d.getGav());
                }
            }
        }
    }
}
