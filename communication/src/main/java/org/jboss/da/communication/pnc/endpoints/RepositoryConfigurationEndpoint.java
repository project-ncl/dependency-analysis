package org.jboss.da.communication.pnc.endpoints;

import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.QUERY_QUERY_PARAM;
import static org.jboss.da.communication.pnc.endpoints.EndpointsParams.SORTING_QUERY_PARAM;
import org.jboss.da.communication.pnc.model.PNCResponseWrapper;
import org.jboss.da.communication.pnc.model.RepositoryConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/repository-configurations")
@Consumes("application/json")
public interface RepositoryConfigurationEndpoint {

    @GET
    Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort, @QueryParam(QUERY_QUERY_PARAM) String q);

    public class RepositoryConfigurationPage extends
            PNCResponseWrapper<List<RepositoryConfiguration>> {
    }
}
