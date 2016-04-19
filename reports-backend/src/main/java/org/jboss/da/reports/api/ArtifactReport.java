package org.jboss.da.reports.api;

import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.model.rest.GAV;

import javax.xml.bind.annotation.XmlTransient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Class, which represents one report for the top-level GAV
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
public class ArtifactReport {

    @Getter
    @NonNull
    @XmlTransient
    private GAV gav;

    @NonNull
    private final Set<String> availableVersions = new HashSet<>();

    @Getter
    @NonNull
    private Optional<String> bestMatchVersion = Optional.empty();

    @NonNull
    private final Set<ArtifactReport> dependencies = new HashSet<>();

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
    private List<ProductVersion> whitelisted;

    public void setBestMatchVersion(Optional<String> version) {
        if (version.isPresent()) {
            availableVersions.add(version.get());
        }
        bestMatchVersion = version;
    }

    public void addAvailableVersion(String version) {
        availableVersions.add(version);
    }

    public void addAvailableVersions(Collection<String> version) {
        availableVersions.addAll(version);
    }

    public void addDependency(ArtifactReport dependency) {
        dependencies.add(dependency);
    }

    public Set<String> getAvailableVersions() {
        return Collections.unmodifiableSet(availableVersions);
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
}
