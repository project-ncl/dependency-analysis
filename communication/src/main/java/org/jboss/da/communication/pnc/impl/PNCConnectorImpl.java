package org.jboss.da.communication.pnc.impl;

import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.authentication.PNCAuthentication;
import org.jboss.da.communication.pnc.authentication.PncAuthenticated;
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
import javax.ws.rs.core.Response.Status;

import java.util.List;

/**
 * Class, which integrates with PNC and process direct calls to PNC REST interface
 * 
 * @author Dustin Kut Moy Cheung <dcheung@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@PncAuthenticated
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

    public ClientRequest getClient(String endpoint) throws ConfigurationParseException {
        ClientRequest request = new ClientRequest(PNC_BASE_URL + endpoint);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer " + pncAuthenticate.getAccessToken());

        return request;
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations() throws Exception {
        ClientResponse<List<BuildConfiguration>> response = getClient("build-configurations").get(
                new GenericType<List<BuildConfiguration>>() {});
        return checkAndReturn(response);
    }

    @Override
    public BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc)
            throws Exception {
        ClientResponse<BuildConfiguration> response = getClient("build-configurations").body(
                MediaType.APPLICATION_JSON, bc).post(BuildConfiguration.class);
        return checkAndReturn(response);
    }

    @Override
    public boolean deleteBuildConfiguration(BuildConfiguration bc) throws Exception {
        return deleteBuildConfiguration(bc.getId());
    }

    @Override
    public boolean deleteBuildConfiguration(int bcId) throws Exception {
        ClientResponse<String> response = getClient("build-configurations/" + bcId).delete(
                String.class);
        return response.getResponseStatus().getStatusCode() == Status.OK.getStatusCode();
    }

    @Override
    public BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws Exception {
        ClientResponse<BuildConfigurationSet> response = getClient("build-configuration-sets")
                .body(MediaType.APPLICATION_JSON, bcs).post(BuildConfigurationSet.class);

        return checkAndReturn(response);
    }

    @Override
    public List<BuildConfigurationSet> getBuildConfigurationSets() throws Exception {
        ClientResponse<List<BuildConfigurationSet>> response = getClient("build-configuration-sets")
                .get(new GenericType<List<BuildConfigurationSet>>() {});
        return checkAndReturn(response);
    }

    @Override
    public List<Product> getProducts() throws Exception {
        ClientResponse<List<Product>> response = getClient("products").get(
                new GenericType<List<Product>>() {});
        return checkAndReturn(response);
    }

    @Override
    public List<Project> getProjects() throws Exception {
        ClientResponse<List<Project>> response = getClient("projects").get(
                new GenericType<List<Project>>() {});
        return checkAndReturn(response);
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
        return checkAndReturn(response);
    }

    private <T> T checkAndReturn(ClientResponse<T> response) throws AuthenticationException {
        if (response.getResponseStatus().getStatusCode() == Status.UNAUTHORIZED.getStatusCode())
            throw new AuthenticationException();
        else
            return response.getEntity();
    }
}
