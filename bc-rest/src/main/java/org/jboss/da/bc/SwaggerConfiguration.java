package org.jboss.da.bc;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@SwaggerDefinition(tags = { @Tag(name = "product", description = "BC generator for product"),
        @Tag(name = "project", description = "BC generator for project") })
public interface SwaggerConfiguration {

}
