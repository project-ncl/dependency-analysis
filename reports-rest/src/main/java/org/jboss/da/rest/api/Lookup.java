package org.jboss.da.rest.api;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.lookup.model.MavenLatestRequest;
import org.jboss.da.lookup.model.MavenLatestResult;
import org.jboss.da.lookup.model.MavenLookupRequest;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupRequest;
import org.jboss.da.lookup.model.NPMLookupResult;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/lookup")
@Tag(name = "lookup")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public interface Lookup {

    @POST
    @Path(value = "/maven")
    @Operation(summary = "Finds best matching versions for given Maven artifact coordinates (GAV).")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = MavenLookupResult.class))))
    Set<MavenLookupResult> lookupMaven(MavenLookupRequest request) throws CommunicationException;

    @POST
    @Path(value = "/maven/latest")
    @Operation(
            summary = "Finds latest matching versions for given Maven artifact coordinates (GAV), including bad versions.",
            description = "This endpoint is used for version increment so it will search all possible places and qualities of artifacts, including deleted and blocklisted artifacts.")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = MavenLatestResult.class))))
    Set<MavenLatestResult> lookupMaven(MavenLatestRequest request) throws CommunicationException;

    @POST
    @Path(value = "/npm")
    @Operation(summary = "Finds best matching versions for given NPM artifact coordinates (name, version).")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NPMLookupResult.class))))
    Set<NPMLookupResult> lookupNPM(NPMLookupRequest request) throws CommunicationException;

}
