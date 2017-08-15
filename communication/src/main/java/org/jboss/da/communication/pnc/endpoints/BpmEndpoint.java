package org.jboss.da.communication.pnc.endpoints;

import org.jboss.da.communication.pnc.model.BPMTask;
import org.jboss.da.communication.pnc.model.PNCResponseWrapper;
import org.jboss.da.communication.pnc.model.RepositoryConfigurationBPMCreate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/bpm")
@Consumes(MediaType.APPLICATION_JSON)
public interface BpmEndpoint {

    @POST
    @Path("/tasks/start-repository-configuration-creation-url-auto")
    Response startRCCreationTask(@HeaderParam("Authorization") String authorization,
            RepositoryConfigurationBPMCreate taskData);

    @GET
    @Path("/tasks/{taskId}")
    Response getBPMTaskById(@PathParam("taskId") int taskId);

    public class BPMTaskSingleton extends PNCResponseWrapper<BPMTask> {
    }

}
