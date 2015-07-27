package org.jboss.da.communication.pnc.impl;

import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.pnc.authentication.PNCAuthentication;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.Project;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PNCConnectorImpl implements PNCConnector {

    private Configuration config;
    private String pncServer;
    private PNCAuthentication pncAuthenticate;

    public PNCConnectorImpl() throws ConfigurationParseException {
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

    @Override
    public List<BuildConfiguration> getBuildConfigurations() throws Exception {
        ClientResponse<BuildConfiguration[]> response = getClient("build-configurations").get(BuildConfiguration[].class);
        return Arrays.asList(response.getEntity());
    }

    @Override
    public List<BuildConfigurationSet> getBuildConfigurationSets() throws Exception {
        ClientResponse<BuildConfigurationSet[]> response = getClient("build-configuration-sets").get(BuildConfigurationSet[].class);
        return Arrays.asList(response.getEntity());
    }

    @Override
    public List<Product> getProducts() throws Exception {
        ClientResponse<Product[]> response = getClient("products").get(Product[].class);
        return Arrays.asList(response.getEntity());
    }

    @Override
    public List<Project> getProjects() throws Exception {
        ClientResponse<Project[]> response = getClient("projects").get(Project[].class);
        return Arrays.asList(response.getEntity());
    }
}
