package org.jboss.da.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@Jacksonized
public class Constraints {

    private final String artifactScope;

    private final List<String> ranks;

    private final String denyList;

    private final String allowList;
}
