package org.jboss.da.rest;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@SwaggerDefinition(tags = {
        @Tag(name = "listings", description = "Listings of black/white listed artifacts"),
        @Tag(name = "reports", description = "Get report of dependencies of projects"),
        @Tag(name = "config", description = "Dependency analyzer configuration APIs") })
public interface SwaggerConfiguration {

}
