package org.jboss.da.model.rest;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@Value
public class Strategy {

    private final String artifactScope;

    private final List<String> ranks;

    private final String denyList;

    private final String allowList;
}
