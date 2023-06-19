package org.jboss.da.rest;

import org.jboss.da.common.Constants;
import org.jboss.da.rest.api.VersionEndpoint;
import org.jboss.pnc.api.dto.ComponentVersion;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;

@ApplicationScoped
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
