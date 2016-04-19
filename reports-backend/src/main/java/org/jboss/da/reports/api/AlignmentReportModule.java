package org.jboss.da.reports.api;

import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class AlignmentReportModule {

    @Getter
    private final GA module;

    @Getter
    private final Map<GAV, Set<ProductArtifact>> internallyBuilt = new HashMap<>();

    @Getter
    private final Map<GAV, Set<ProductArtifact>> differentVersion = new HashMap<>();

    @Getter
    private final Set<GAV> notBuilt = new HashSet<>();

    @Getter
    private final Set<GAV> blacklisted = new HashSet<>();

    public AlignmentReportModule(GA module) {
        this.module = module;
    }

}
