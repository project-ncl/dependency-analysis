package org.jboss.da.rest.api;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.lookup.model.MavenLookupRequest;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupRequest;
import org.jboss.da.lookup.model.NPMLookupResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/lookup")
@Api(value = "lookup")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public interface Lookup {

    @POST
    @Path(value = "/maven")
    @ApiOperation(
            value = "Finds best matching versions for given Maven artifact coordinates (GAV).",
            responseContainer = "List",
            response = MavenLookupResult.class)
    Set<MavenLookupResult> lookupMaven(MavenLookupRequest request) throws CommunicationException;

    @POST
    @Path(value = "/npm")
    @ApiOperation(
            value = "Finds best matching versions for given NPM artifact coordinates (name, version).",
            responseContainer = "List",
            response = MavenLookupResult.class)
    Set<NPMLookupResult> lookupNPM(NPMLookupRequest request) throws CommunicationException;

}
