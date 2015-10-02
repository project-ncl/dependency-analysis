package org.jboss.da.communication.pnc.impl;

import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.authentication.PNCAuthentication;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.Project;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
public class PNCConnectorImpl implements PNCConnector {

    @Inject
    private PNCAuthentication pncAuthenticate;

    private final String PNC_BASE_URL;

    @Deprecated
    public PNCConnectorImpl() {
        PNC_BASE_URL = "";
    }

    @Inject
    public PNCConnectorImpl(Configuration config) throws ConfigurationParseException {
        PNC_BASE_URL = config.getConfig().getPncServer() + "/pnc-rest/rest/";
    }

    private ClientRequest getClient(String endpoint, boolean authenticate)
            throws ConfigurationParseException {
        ClientRequest request = new ClientRequest(PNC_BASE_URL + endpoint);
        request.accept(MediaType.APPLICATION_JSON);

        checkAutentication(authenticate, request);

        return request;
    }

    private void checkAutentication(boolean authenticate, ClientRequest request) {
        // TODO: instead of getting a new token every time, check if existing
        // TODO: token is expired before asking for a new one
        if (authenticate) {
            String token = pncAuthenticate.authenticate();
            request.header("Authorization", "Bearer " + token);
        }
    }

    public ClientRequest getClient(String endpoint) throws ConfigurationParseException {
        return getClient(endpoint, false);
    }

    public ClientRequest getAuthenticatedClient(String endpoint) throws ConfigurationParseException {
        return getClient(endpoint, true);
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations() throws Exception {
        ClientResponse<List<BuildConfiguration>> response = getClient("build-configurations").get(
                new GenericType<List<BuildConfiguration>>() {});
        return response.getEntity();
    }

    @Override
    public BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc)
            throws Exception {
        ClientResponse<BuildConfiguration> response = getClient("build-configurations").body(
                MediaType.APPLICATION_JSON, bc).post(BuildConfiguration.class);
        return response.getEntity();
    }

    @Override
    public BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws Exception {
        ClientResponse<BuildConfigurationSet> response = getClient("build-configuration-sets")
                .body(MediaType.APPLICATION_JSON, bcs).post(BuildConfigurationSet.class);
        return response.getEntity();
    }

    @Override
    public List<BuildConfigurationSet> getBuildConfigurationSets() throws Exception {
        ClientResponse<List<BuildConfigurationSet>> response = getClient("build-configuration-sets")
                .get(new GenericType<List<BuildConfigurationSet>>() {});
        return response.getEntity();
    }

    @Override
    public List<Product> getProducts() throws Exception {
        ClientResponse<List<Product>> response = getClient("products").get(
                new GenericType<List<Product>>() {});
        return response.getEntity();
    }

    @Override
    public List<Project> getProjects() throws Exception {
        ClientResponse<List<Project>> response = getClient("projects").get(
                new GenericType<List<Project>>() {});
        return response.getEntity();
    }

    @Override
    public BuildConfigurationSet findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws Exception {
        ClientResponse<List<BuildConfiguration>> response = getClient(
                String.format("build-configurations?q=scmRepoURL=='%s';scmRevision=='%s'", scmUrl,
                        scmRevision)).get(new GenericType<List<BuildConfiguration>>() {});
        return response.getEntity();
    }

}
