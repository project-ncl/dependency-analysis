package org.jboss.da.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.api.dto.ComponentVersion;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/version")
@Tag(name = "version")
public interface VersionEndpoint {
    /**
     * Return the current version of Dependency Analysis
     *
     * @return version of DA
     */
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Get version of Dependency-Analysis")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ComponentVersion.class)))
    public ComponentVersion getVersion();
}
