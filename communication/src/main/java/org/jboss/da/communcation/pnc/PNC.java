package org.jboss.da.communcation.pnc;

import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communcation.pnc.authentication.PNCAuthentication;
import org.jboss.da.communcation.pnc.model.BuildConfiguration;
import org.jboss.da.communcation.pnc.model.BuildConfigurationSet;
import org.jboss.da.communcation.pnc.model.Product;
import org.jboss.da.communcation.pnc.model.Project;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

public class PNC {

    private Configuration config;
    private String pncServer;
    private PNCAuthentication pncAuthenticate;

    public PNC() throws ConfigurationParseException {
        config = new Configuration();
        pncServer = config.getConfig().getPncServer();
        pncAuthenticate = new PNCAuthentication();
    }

    public ClientRequest getClient(String endpoint, boolean authenticate) {
        ClientRequest request = new ClientRequest(pncServer + "/pnc-rest/rest/" + endpoint);
        request.accept(MediaType.APPLICATION_JSON);

        // TODO: instead of getting a new token everytime, check if existing
        // TODO: token is expired before asking for a new one
        if (authenticate) {
            String token = pncAuthenticate.authenticate();
            request.header("Authorization", "Bearer " + token);
        }

        return request;
    }

    public ClientRequest getClient(String endpoint) {
        return getClient(endpoint, false);
    }

    public List<BuildConfiguration> getBuildConfigurations() throws Exception {
        ClientResponse<BuildConfiguration[]> response = getClient("build-configurations").get(BuildConfiguration[].class);
        return Arrays.asList(response.getEntity());
    }

    public List<BuildConfigurationSet> getBuildConfigurationSets() throws Exception {
        ClientResponse<BuildConfigurationSet[]> response = getClient("build-configuration-sets").get(BuildConfigurationSet[].class);
        return Arrays.asList(response.getEntity());
    }

    public List<Product> getProducts() throws Exception {
        ClientResponse<Product[]> response = getClient("products").get(Product[].class);
        return Arrays.asList(response.getEntity());
    }

    public List<Project> getProjects() throws Exception {
        ClientResponse<Project[]> response = getClient("projects").get(Project[].class);
        return Arrays.asList(response.getEntity());
    }
}
