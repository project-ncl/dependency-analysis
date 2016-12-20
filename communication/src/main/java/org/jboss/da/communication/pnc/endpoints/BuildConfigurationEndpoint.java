package org.jboss.da.communication.pnc.endpoints;

import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.QUERY_QUERY_PARAM;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.SORTING_QUERY_PARAM;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.PNCResponseWrapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.ValidationException;

import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/build-configurations")
@Consumes("application/json")
public interface BuildConfigurationEndpoint {

    @GET
    Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort, @QueryParam(QUERY_QUERY_PARAM) String q);

    @DELETE
    @Path("/{id}")
    Response deleteSpecific(@HeaderParam("Authorization") String authorization,
            @PathParam("id") Integer id);

    @PUT
    @Path("/{id}")
    public Response update(@HeaderParam("Authorization") String authorization,
            @PathParam("id") Integer id, BuildConfiguration buildConfigurationRest);

    public class BuildConfigurationPage extends PNCResponseWrapper<List<BuildConfiguration>> {
    }
}
