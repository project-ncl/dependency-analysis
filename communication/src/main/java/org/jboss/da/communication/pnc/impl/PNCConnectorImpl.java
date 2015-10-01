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

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class PNCConnectorImpl implements PNCConnector {

    @Inject
    private PNCAuthentication pncAuthenticate;

    private String pncBaseUrl;

    @Deprecated
    public PNCConnectorImpl() {
    }

    @Inject
    public PNCConnectorImpl(Configuration config) throws ConfigurationParseException {
        pncBaseUrl = config.getConfig().getPncServer() + "/pnc-rest/rest/";
    }

    private ClientRequest getClient(String endpoint, boolean authenticate)
            throws ConfigurationParseException {
        ClientRequest request = new ClientRequest(pncBaseUrl + endpoint);
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
        ClientResponse<BuildConfiguration[]> response = getClient("build-configurations").get(
                BuildConfiguration[].class);
        return Arrays.asList(response.getEntity());
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
        ClientResponse<BuildConfigurationSet[]> response = getClient("build-configuration-sets")
                .get(BuildConfigurationSet[].class);
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
