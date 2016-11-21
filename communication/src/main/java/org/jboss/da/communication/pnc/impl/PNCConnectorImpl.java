package org.jboss.da.communication.pnc.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCAuthConnector;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.endpoints.BpmEndpoint;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationBPMCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;

import org.jboss.da.communication.pnc.model.ProductVersion;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationEndpoint;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationEndpoint.BuildConfigurationPage;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationSetEndpoint;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationSetEndpoint.BuildConfigurationSetPage;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationSetEndpoint.BuildConfigurationSetSingleton;
import org.jboss.da.communication.pnc.endpoints.ProductVersionEndpoint;
import org.jboss.da.communication.pnc.endpoints.ProductVersionEndpoint.ProductVersionPage;
import org.jboss.da.communication.pnc.endpoints.ProductVersionEndpoint.ProductVersionSingleton;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_OK;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_CREATED;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_UNAUTHORIZED;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_FORBIDDEN;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_NO_CONTENT;

import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class PNCConnectorImpl implements PNCAuthConnector {

    private final ResteasyWebTarget target;

    private final String token;

    public PNCConnectorImpl(ResteasyWebTarget target, String token) {
        this.target = target;
        this.token = token;
    }

    public PNCConnectorImpl(ResteasyWebTarget target) {
        this(target, null);
    }

    private String getAccessToken() throws CommunicationException {
        if (token == null) {
            throw new CommunicationException("Current user not authenticated.");
        }
        return "Bearer " + token;
    }

    private <T> T getEndpoint(Class<T> type) {
        return target.proxy(type);
    }

    private List<BuildConfiguration> getBuildConfigurations(String query) throws CommunicationException,
            PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);

        Response response = endpoint.getAll(0, 5000, "", query);
        Optional<BuildConfigurationPage> wrapper = checkAndReturn(response, BuildConfigurationPage.class);

        return wrapper.map(w -> w.getContent()).orElse(Collections.emptyList());
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException {
        String query = String.format("(scmRepoURL=='%s';scmRevision=='%s'),"
                + "(scmExternalRepoURL=='%s';scmExternalRevision=='%s')", scmUrl, scmRevision,
                scmUrl, scmRevision);
        return getBuildConfigurations(query);
    }

    @Override
    public Optional<BuildConfiguration> getBuildConfiguration(String name)
            throws CommunicationException, PNCRequestException {
        String query = String.format("name=='%s'", name);
        return single(getBuildConfigurations(query), "build configuration for name %s", name);
    }

    @Override
    public void createBuildConfiguration(BuildConfigurationBPMCreate bc)
            throws CommunicationException, PNCRequestException {
        BpmEndpoint endpoint = getEndpoint(BpmEndpoint.class);
        checkCode(endpoint.startBCCreationTask(getAccessToken(), bc));
    }

    @Override
    public void updateBuildConfiguration(BuildConfiguration bc) throws CommunicationException,
            PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);
        checkCode(endpoint.update(getAccessToken(), bc.getId(), bc));
    }

    @Override
    public void deleteBuildConfiguration(BuildConfiguration bc) throws CommunicationException,
            PNCRequestException {
        deleteBuildConfiguration(bc.getId());
    }

    @Override
    public void deleteBuildConfiguration(int bcId) throws CommunicationException,
            PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);
        checkCode(endpoint.deleteSpecific(getAccessToken(), bcId));
    }

    @Override
    public BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws CommunicationException, PNCRequestException {
        BuildConfigurationSetEndpoint endpoint = getEndpoint(BuildConfigurationSetEndpoint.class);
        Response response = endpoint.createNew(getAccessToken(), bcs);
        return checkAndReturn(response, BuildConfigurationSetSingleton.class)
                .map(s -> s.getContent())
                .orElseThrow(() -> new PNCRequestException("PNC didn't return created object"));
    }

    @Override
    public Optional<BuildConfigurationSet> findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds) throws CommunicationException, PNCRequestException {
        String commaDelimitedIds = buildConfigurationIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String query = String.format("productVersion.id==%s;buildConfigurations.id=in=(%s)",
                productVersionId, commaDelimitedIds);

        BuildConfigurationSetEndpoint endpoint = getEndpoint(BuildConfigurationSetEndpoint.class);
        Response response = endpoint.getAll(0, 2, "", query);
        List<BuildConfigurationSet> bcss = checkAndReturn(response, BuildConfigurationSetPage.class)
                .map(s -> s.getContent())
                .orElse(Collections.emptyList());

        return single(bcss, "build configuration set for product version %d", productVersionId);
    }

    @Override
    public Optional<ProductVersion> findProductVersion(int productId, String version)
            throws CommunicationException, PNCRequestException {
        String query = String.format("productId==%s;version=='%s'", productId, version);

        ProductVersionEndpoint endpoint = getEndpoint(ProductVersionEndpoint.class);
        Response response = endpoint.getAll(0, 2, "", query);
        List<ProductVersion> pvs = checkAndReturn(response, ProductVersionPage.class)
                .map(s -> s.getContent())
                .orElse(Collections.emptyList());

        return single(pvs, "product version for product %d and version %s", productId, version);
    }

    @Override
    public ProductVersion createProductVersion(ProductVersion pv) throws CommunicationException,
            PNCRequestException {

        ProductVersionEndpoint endpoint = getEndpoint(ProductVersionEndpoint.class);
        Response response = endpoint.createNew(getAccessToken(), pv);
        return checkAndReturn(response, ProductVersionSingleton.class)
                .map(s -> s.getContent())
                .orElseThrow(() -> new PNCRequestException("PNC didn't return created object"));
    }

    private <T> Optional<T> checkAndReturn(Response response, Class<T> type)
            throws AuthenticationException, PNCRequestException, CommunicationException {
        try {
            switch (response.getStatus()) {
                case SC_OK:
                case SC_CREATED:
                    return Optional.of(response.readEntity(type));
                case SC_NO_CONTENT:
                    return Optional.empty();
                case SC_UNAUTHORIZED:
                case SC_FORBIDDEN:
                    throw new CommunicationException("Current user not authenticated.");
                default:
                    throw new PNCRequestException(response.getStatusInfo() + " "
                            + response.readEntity(String.class));
            }
        } finally {
            response.close();
        }
    }

    private void checkCode(Response response) throws AuthenticationException, PNCRequestException,
            CommunicationException {
        try {
            switch (response.getStatus()) {
                case SC_OK:
                case SC_CREATED:
                case SC_NO_CONTENT:
                    return;
                case SC_UNAUTHORIZED:
                case SC_FORBIDDEN:
                    throw new CommunicationException("Current user not authenticated.");
                default:
                    throw new PNCRequestException(response.getStatus() + " "
                            + response.readEntity(String.class));
            }
        } finally {
            response.close();
        }
    }

    private <T> Optional<T> single(List<T> list, String errorMessage, Object... args)
            throws PNCRequestException {
        if (list.size() > 1) {
            throw new PNCRequestException(String.format("Expected single " + errorMessage
                    + ". Got multiple.", args));
        }
        return list.stream().findAny();
    }

}
