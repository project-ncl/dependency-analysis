package org.jboss.da.reports.api;

import org.jboss.da.model.rest.GAV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Class, which represents one report for the top-level GAV
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RequiredArgsConstructor
public class ArtifactReport implements Comparable<ArtifactReport> {

    @Getter
    @NonNull
    private final Set<Long> ProductVersions = new HashSet<>();

    @Getter
    @NonNull
    private GAV gav;

    @NonNull
    @Setter
    private List<String> availableVersions = new ArrayList<>();

    @Getter
    @NonNull
    private Optional<String> bestMatchVersion = Optional.empty();

    @NonNull
    private final Set<ArtifactReport> dependencies = new TreeSet<>();

    /**
     * Indicator if the artifact was blacklisted
     */
    @Getter
    @Setter
    private boolean blacklisted;

    /**
     * List of product where this artifact is whitelisted
     */
    @Getter
    @Setter
    private List<org.jboss.da.products.api.Product> whitelisted;

    public void setBestMatchVersion(Optional<String> version) {
        bestMatchVersion = version;
    }

    public void addDependency(ArtifactReport dependency) {
        dependencies.add(dependency);
    }

    public List<String> getAvailableVersions() {
        return Collections.unmodifiableList(availableVersions);
    }

    public Set<ArtifactReport> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public String getGroupId() {
        return gav.getGroupId();
    }

    public String getArtifactId() {
        return gav.getArtifactId();
    }

    public String getVersion() {
        return gav.getVersion();
    }

    /**
     * Returns true if all the dependencies of this artifact have a GAV already in PNC/Brew.
     * @return true if all the dependencies of this artifact have a GAV already in PNC/Brew.
     */
    public boolean isDependencyVersionSatisfied() {
        return getNotBuiltDependencies() == 0;
    }

    public int getNotBuiltDependencies() {
        return dependencies.stream().mapToInt((dependency) -> {
            int number = dependency.getNotBuiltDependencies();
            if(!dependency.bestMatchVersion.isPresent()){
                number ++;
            }
            return number;
        }).sum();
    }

    @Override
    public int compareTo(ArtifactReport o) {
        return this.gav.compareTo(o.gav);
    }
}
