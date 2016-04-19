package org.jboss.da.communication.pom.impl;

import org.commonjava.maven.atlas.graph.rel.DependencyRelationship;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.GalleyWrapper;
import org.jboss.da.model.rest.GAV;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@ApplicationScoped
public class DependencyTreeBuilder {

    @Inject
    private Logger log;

    /**
     * Transforms DependencyRelationships into GAVDependencyTree.
     */
    public GAVDependencyTree getDependencyTree(Set<DependencyRelationship> rels, GAV origin, boolean testDeps, boolean providedDeps){
        PathNode pathRoot = new PathNode(origin);
        Map<GAV, Set<DependencyRelationship>> byGav = new HashMap<>();

        for(DependencyRelationship rel : rels){
            GAV gav = GalleyWrapper.generateGAV(rel.getDeclaring());
            Set<DependencyRelationship> deps = byGav.computeIfAbsent(gav, (k) -> new HashSet<>());
            deps.add(rel);
        }
        
        walkDependencies(pathRoot, byGav, testDeps, providedDeps);

        GAVDependencyTree depRoot = new GAVDependencyTree(pathRoot.gav);
        constructDepTree(depRoot, pathRoot);
        depRoot.prune();
        return depRoot;
    }

    private void constructDepTree(GAVDependencyTree depTree, PathNode pathTree) {
        for (PathNode child : pathTree.childs) {
            GAVDependencyTree d = new GAVDependencyTree(child.gav);
            depTree.addDependency(d);
            constructDepTree(d, child);
        }
    }

    private void walkDependencies(PathNode root, Map<GAV, Set<DependencyRelationship>> byGav,
            boolean testDeps, boolean providedDeps) {
        Set<DependencyRelationship> rels = byGav.get(root.gav);

        if (rels == null)
            return;

        for (DependencyRelationship d : rels) {
            ArtifactRef target = d.getTarget();
            GAV targetGav = GalleyWrapper.generateGAV(target);

            if (root.exclude(target))
                continue; // Avoid excluded

            PathNode pn = new PathNode(root, targetGav);
            pn.addExcludes(d.getExcludes());

            root.addChild(pn);

            if (pn.parents.contains(targetGav)) {
                log.warn("Found cyclic dependency: " + root.gav + " -> " + targetGav);
                continue; // Avoid cycle
            }

            // When test (provided) dependencies are ommited, GalleyWrapper
            // should handle this in most cases when retrieving relationships,
            // however in scenario like this:
            // ' a
            // ' |- b test
            // ' | |- c compile
            // ' |- d compile
            // ' |- b compile
            // ' |- c compile
            // GalleyWrapper will return relationship b->c (because of path
            // a-d-b) and we must ensure that path a-b will ommit c when test
            // (provided) dependencies are to be ommited.
            if (!GalleyWrapper.shouldAnalyzeDependencies(d, testDeps, providedDeps))
                continue;

            walkDependencies(pn, byGav, testDeps, providedDeps);
        }

    }

    private static class PathNode {

        private final Set<GAV> parents = new HashSet<>();

        private final Set<ProjectRef> excludes = new HashSet<>();

        private final GAV gav;

        private final Set<PathNode> childs = new HashSet<>();

        public PathNode(GAV gav) {
            this.gav = gav;
        }

        public PathNode(PathNode parent, GAV gav) {
            parents.addAll(parent.parents);
            parents.add(parent.gav);
            excludes.addAll(parent.excludes);
            this.gav = gav;
        }

        public void addExcludes(Set<ProjectRef> excludes) {
            this.excludes.addAll(excludes);
        }

        public boolean exclude(ProjectRef ref){
            return excludes.stream().anyMatch(ex -> ex.matches(ref));
        }

        public void addChild(PathNode pn) {
            childs.add(pn);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.parents);
            hash = 89 * hash + Objects.hashCode(this.gav);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PathNode other = (PathNode) obj;
            if (!Objects.equals(this.parents, other.parents)) {
                return false;
            }
            if (!Objects.equals(this.gav, other.gav)) {
                return false;
            }
            return true;
        }
    }
}
