package org.jboss.da.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.pnc.api.dto.ComponentVersion;

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
    @APIResponse(content = @Content(schema = @Schema(implementation = ComponentVersion.class)))
    ComponentVersion getVersion();
}
