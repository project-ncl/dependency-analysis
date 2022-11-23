package org.jboss.da.rest.api.v1;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Dependency Analyzer",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")),
        servers = { @Server(url = "/da", description = "Dependency Analyzer") },
        tags = {
                @Tag(name = "lookup", description = "Lookup of artifact versions."),
                @Tag(name = "blocklist", description = "Listings of blocklisted artifacts"),
                @Tag(name = "reports", description = "Get report of dependencies of projects"),
                @Tag(name = "deprecated", description = "Deprecated endpoints.") })
public interface SwaggerConfiguration {

}
