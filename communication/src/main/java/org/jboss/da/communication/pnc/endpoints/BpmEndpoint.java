package org.jboss.da.communication.pnc.endpoints;

import org.jboss.da.communication.pnc.model.BuildConfigurationBPMCreate;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@Path("/bpm")
@Consumes(MediaType.APPLICATION_JSON)
public interface BpmEndpoint {

    @POST
    @Path("/tasks/start-build-configuration-creation")
    Response startBCCreationTask(@HeaderParam("Authorization") String authorization,
            BuildConfigurationBPMCreate taskData);
}
