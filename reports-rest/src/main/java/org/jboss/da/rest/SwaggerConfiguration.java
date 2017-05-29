package org.jboss.da.rest;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 *
 * @author Honza Brázdil &lt;janinko.g@gmail.com&gt;
 */
@SwaggerDefinition(tags = {
        @Tag(name = "listings", description = "Listings of white listed artifacts"),
        @Tag(name = "blacklist", description = "Listings of blacklisted artifacts"),
        @Tag(name = "reports", description = "Get report of dependencies of projects"),
        @Tag(name = "config", description = "Dependency analyzer configuration APIs") })
public interface SwaggerConfiguration {

}
