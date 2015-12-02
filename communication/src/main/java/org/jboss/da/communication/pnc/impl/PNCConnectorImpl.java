package org.jboss.da.communication.pnc.impl;

import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.authentication.PNCAuthentication;
import org.jboss.da.communication.pnc.authentication.PncAuthenticated;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.PNCResponseWrapper;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.ProductVersion;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public ClientRequest getClient(String endpoint, String accessToken)
            throws ConfigurationParseException {
        ClientRequest request = new ClientRequest(PNC_BASE_URL + endpoint);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer " + accessToken);

        return request;
    }

    private <T> ClientResponse<T> get(String url, GenericType type, String accessToken)
            throws CommunicationException {
        try {
            ClientRequest request = getClient(url, accessToken);
            return request.get(type);
        } catch (Exception ex) {
            throw new CommunicationException("Error while contacting PNC", ex);
        }
    }

    private <T> ClientResponse<T> post(String url, Object body, GenericType type, String accessToken)
            throws CommunicationException {
        try {
            ClientRequest request = getClient(url, accessToken);
            return request.body(MediaType.APPLICATION_JSON, body).post(type);
        } catch (Exception ex) {
            throw new CommunicationException("Error while contacting PNC", ex);
        }
    }

    private <T> ClientResponse<T> delete(String url, Class type, String accessToken)
            throws CommunicationException {
        try {
            ClientRequest request = getClient(url, accessToken);
            return request.delete(type);
        } catch (Exception ex) {
            throw new CommunicationException("Error while contacting PNC", ex);
        }
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations() throws CommunicationException,
            PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        ClientResponse<PNCResponseWrapper<List<BuildConfiguration>>> response = get(
                "build-configurations?pageIndex=0&pageSize=5000", // TODO solve pagination
                new GenericType<PNCResponseWrapper<List<BuildConfiguration>>>() {}, accessToken);
        return checkAndReturn(response, accessToken).getContent();
    }

    @Override
    public BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc)
            throws CommunicationException, PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        ClientResponse<PNCResponseWrapper<BuildConfiguration>> response = post(
                "build-configurations", bc,
                new GenericType<PNCResponseWrapper<BuildConfiguration>>() {}, accessToken);
        return checkAndReturn(response, accessToken).getContent();
    }

    @Override
    public boolean deleteBuildConfiguration(BuildConfiguration bc) throws CommunicationException,
            PNCRequestException {
        return deleteBuildConfiguration(bc.getId());
    }

    @Override
    public boolean deleteBuildConfiguration(int bcId) throws CommunicationException,
            PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        ClientResponse<String> response = delete("build-configurations/" + bcId, String.class,
                accessToken);
        return checkReturnCode(response, accessToken);
    }

    @Override
    public BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws CommunicationException, PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        ClientResponse<PNCResponseWrapper<BuildConfigurationSet>> response = post(
                "build-configuration-sets", bcs,
                new GenericType<PNCResponseWrapper<BuildConfigurationSet>>() {}, accessToken);
        return checkAndReturn(response, accessToken).getContent();
    }

    @Override
    public Optional<BuildConfigurationSet> findBuildConfigurationSet(int productVersionId,
                                                                     List<Integer> buildConfigurationIds)
            throws CommunicationException, PNCRequestException {

        String accessToken = pncAuthenticate.getAccessToken();
        String commaDelimitedIds = buildConfigurationIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String requestUrl = String
                .format(
                        "build-configuration-sets?q=productVersion.id==%s;buildConfigurations.id=in=(%s)",
                        productVersionId, commaDelimitedIds);

        ClientResponse<PNCResponseWrapper<List<BuildConfigurationSet>>> response =
                get(requestUrl,
                    new GenericType<PNCResponseWrapper<List<BuildConfigurationSet>>>() {},
                    accessToken);

        return processFindResponse(response, accessToken);
    }

    @Override
    public Optional<ProductVersion> findProductVersion(Product p, String version)
            throws CommunicationException, PNCRequestException {
        return findProductVersion(p.getId(), version);
    }

    @Override
    public Optional<ProductVersion> findProductVersion(int productId, String version)
            throws CommunicationException, PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        String requestUrl = String.format("product-versions?q=product.id==%s;version=='%s'",
                productId, version);
        ClientResponse<PNCResponseWrapper<List<ProductVersion>>> response = get(requestUrl,
                new GenericType<PNCResponseWrapper<List<ProductVersion>>>() {}, accessToken);

        return processFindResponse(response, accessToken);
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        String requestUrl = String
                .format(
                // TODO solve pagination
                "build-configurations?q=scmRepoURL=='%s';scmRevision=='%s'&pageIndex=0&pageSize=500",
                        scmUrl, scmRevision);

        ClientResponse<PNCResponseWrapper<List<BuildConfiguration>>> response = get(requestUrl,
                new GenericType<PNCResponseWrapper<List<BuildConfiguration>>>() {}, accessToken);

        if (response.getEntity() == null && response.getResponseStatus() == Status.NO_CONTENT)
            return Collections.emptyList();
        else
            return checkAndReturn(response, accessToken).getContent();
    }

    @Override
    public Optional<Product> findProduct(String name) throws CommunicationException,
            PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        String requestUrl = String.format("products?q=name=='%s'", name);
        ClientResponse<PNCResponseWrapper<List<Product>>> response = get(requestUrl,
                new GenericType<PNCResponseWrapper<List<Product>>>() {}, accessToken);

        return processFindResponse(response, accessToken);
    }

    @Override
    public Product createProduct(Product p) throws CommunicationException, PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        ClientResponse<PNCResponseWrapper<Product>> response = post("products", p,
                new GenericType<PNCResponseWrapper<Product>>() {}, accessToken);

        return checkAndReturn(response, accessToken).getContent();
    }

    @Override
    public ProductVersion createProductVersion(ProductVersion pv) throws CommunicationException,
            PNCRequestException {
        String accessToken = pncAuthenticate.getAccessToken();
        ClientResponse<PNCResponseWrapper<ProductVersion>> response = post("product-versions", pv,
                new GenericType<PNCResponseWrapper<ProductVersion>>() {}, accessToken);
        return checkAndReturn(response, accessToken).getContent();
    }

    private <T> T checkAndReturn(ClientResponse<T> response, String accessToken)
            throws AuthenticationException, PNCRequestException {
        switch (response.getResponseStatus()) {
            case OK:
            case CREATED:
                return response.getEntity();
            case UNAUTHORIZED:
                throw new AuthenticationException(accessToken);
            default:
                throw new PNCRequestException(response.getResponseStatus() + " "
                        + response.getEntity(String.class));
        }

    }

    private boolean checkReturnCode(ClientResponse<String> response, String accessToken)
            throws AuthenticationException, PNCRequestException {
        switch (response.getResponseStatus()) {
            case OK:
                return true;
            case UNAUTHORIZED:
                throw new AuthenticationException(accessToken);
            default:
                throw new PNCRequestException(response.getResponseStatus() + " "
                        + response.getEntity(String.class));
        }
    }

    private <T> Optional<T> processFindResponse(
            ClientResponse<PNCResponseWrapper<List<T>>> response, String accessToken)
            throws AuthenticationException, PNCRequestException {

        // if there is no product with that name on PNC, the response code
        // will be NO_CONTENT
        if (response.getResponseStatus().equals(Status.NO_CONTENT)) {
            return Optional.empty();
        } else {
            // since the name of a product is unique, there'll only be one
            // item in the list
            List<T> items = checkAndReturn(response, accessToken).getContent();
            return Optional.of(items.get(0));
        }
    }
}
