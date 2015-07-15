package org.jboss.da.communcation.pnc;

import org.jboss.da.common.json.DAConfig;
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
    private DAConfig conf;
    private String pncServer;
    private String token;

    public PNC() throws ConfigurationParseException {
        Configuration config = new Configuration();
        DAConfig conf = config.getConfig();
        pncServer = config.getConfig().getPncServer();

        PNCAuthentication pncAuthenticate = new PNCAuthentication();
        token = pncAuthenticate.authenticate();
    }

    public ClientRequest getClient(String endpoint, boolean authenticate) {
        ClientRequest request = new ClientRequest(pncServer + "/pnc-rest/rest/" + endpoint);
        request.accept(MediaType.APPLICATION_JSON);

        if (authenticate) {
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
