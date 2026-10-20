package org.jboss.da.rest;

import java.time.ZonedDateTime;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.da.common.Constants;
import org.jboss.da.rest.api.VersionEndpoint;
import org.jboss.pnc.api.dto.ComponentVersion;

@ApplicationScoped
@PermitAll
public class VersionEndpointImpl implements VersionEndpoint {
    public ComponentVersion getVersion() {
        return ComponentVersion.builder()
                .name("Dependency Analysis")
                .version(Constants.DA_VERSION)
                .commit(Constants.COMMIT_HASH)
                .builtOn(ZonedDateTime.parse(Constants.BUILD_TIME))
                .build();
    }
}
