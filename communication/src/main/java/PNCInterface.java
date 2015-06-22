import org.jboss.da.communcation.pnc.entity.BuildConfiguration;
import org.jboss.da.communcation.pnc.entity.BuildConfigurationSet;
import org.jboss.da.communcation.pnc.entity.Product;
import org.jboss.da.communcation.pnc.entity.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public interface PNCInterface {

    @GET
    @Path("pnc-rest/rest/build-configurations")
    @Produces(MediaType.APPLICATION_JSON)
    List<BuildConfiguration> getBuildConfigurations();

    @GET
    @Path("pnc-rest/rest/build-configuration-sets")
    @Produces(MediaType.APPLICATION_JSON)
    List<BuildConfigurationSet> getBuildConfigurationSets();

    @GET
    @Path("pnc-rest/rest/products")
    @Produces(MediaType.APPLICATION_JSON)
    List<Product> getProducts();

    @GET
    @Path("pnc-rest/rest/projects")
    @Produces(MediaType.APPLICATION_JSON)
    List<Project> getProjects();
}
