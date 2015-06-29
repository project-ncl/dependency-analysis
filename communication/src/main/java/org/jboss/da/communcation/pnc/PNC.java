package org.jboss.da.communcation.pnc;

import org.jboss.da.communcation.pnc.entity.BuildConfiguration;
import org.jboss.da.communcation.pnc.entity.BuildConfigurationSet;
import org.jboss.da.communcation.pnc.entity.Product;
import org.jboss.da.communcation.pnc.entity.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("pnc-rest/rest")
public interface PNC {

    @GET
    @Path("build-configurations")
    @Produces(MediaType.APPLICATION_JSON)
    List<BuildConfiguration> getBuildConfigurations();

    @GET
    @Path("build-configuration-sets")
    @Produces(MediaType.APPLICATION_JSON)
    List<BuildConfigurationSet> getBuildConfigurationSets();

    @GET
    @Path("products")
    @Produces(MediaType.APPLICATION_JSON)
    List<Product> getProducts();

    @GET
    @Path("projects")
    @Produces(MediaType.APPLICATION_JSON)
    List<Project> getProjects();
}
