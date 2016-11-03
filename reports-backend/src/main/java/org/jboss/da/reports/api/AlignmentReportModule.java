package org.jboss.da.reports.api;

import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Getter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class AlignmentReportModule {

    @Getter
    private final GA module;

    @Getter
    private final Map<GAV, Set<ProductArtifact>> internallyBuilt = new TreeMap<>();

    @Getter
    private final Map<GAV, Set<ProductArtifact>> differentVersion = new TreeMap<>();

    @Getter
    private final Set<GAV> notBuilt = new TreeSet<>();

    @Getter
    private final Set<GAV> blacklisted = new TreeSet<>();

    public AlignmentReportModule(GA module) {
        this.module = module;
    }

}
