package org.jboss.da.communication.pnc.impl;

import org.jboss.da.communication.pnc.api.PNCAuthConnector;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.endpoints.BpmEndpoint;
import org.jboss.da.communication.pnc.endpoints.BpmEndpoint.BPMTaskSingleton;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;

import org.jboss.da.communication.pnc.model.ProductVersion;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationEndpoint;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationEndpoint.BuildConfigurationPage;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationEndpoint.BuildConfigurationSingleton;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationSetEndpoint;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationSetEndpoint.BuildConfigurationSetPage;
import org.jboss.da.communication.pnc.endpoints.BuildConfigurationSetEndpoint.BuildConfigurationSetSingleton;
import org.jboss.da.communication.pnc.endpoints.ProductVersionEndpoint;
import org.jboss.da.communication.pnc.endpoints.ProductVersionEndpoint.ProductVersionPage;
import org.jboss.da.communication.pnc.endpoints.ProductVersionEndpoint.ProductVersionSingleton;
import org.jboss.da.communication.pnc.endpoints.RepositoryConfigurationEndpoint;
import org.jboss.da.communication.pnc.endpoints.RepositoryConfigurationEndpoint.RepositoryConfigurationPage;
import org.jboss.da.communication.pnc.model.BPMTask;
import org.jboss.da.communication.pnc.model.BpmNotification;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.communication.pnc.model.RepositoryConfiguration;
import org.jboss.da.communication.pnc.model.RepositoryConfigurationConflict;
import org.jboss.da.communication.pnc.model.RepositoryConfigurationBPMCreate;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_CONFLICT;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_OK;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_CREATED;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_UNAUTHORIZED;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_FORBIDDEN;
import static org.jboss.resteasy.util.HttpResponseCodes.SC_NO_CONTENT;

import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
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

    private String getAccessToken() {
        if (token == null) {
            throw new IllegalStateException("Current user not authenticated.");
        }
        return "Bearer " + token;
    }

    private <T> T getEndpoint(Class<T> type) {
        return target.proxy(type);
    }

    @Override
    public List<RepositoryConfiguration> getRepositoryConfigurations(String url) throws PNCRequestException {
        String query = String.format("internalScmRepoUrl=='%s',externalScmRepoUrl=='%s')", url, url);
        RepositoryConfigurationEndpoint endpoint = getEndpoint(RepositoryConfigurationEndpoint.class);

        Response response = endpoint.getAll(0, 5000, "", query);
        Optional<RepositoryConfigurationPage> wrapper = checkAndReturn(response, RepositoryConfigurationPage.class);

        return wrapper.map(w -> w.getContent()).orElse(Collections.emptyList());
    }

    private List<BuildConfiguration> getBuildConfigurations(String query) throws PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);

        Response response = endpoint.getAll(0, 5000, "", query);
        Optional<BuildConfigurationPage> wrapper = checkAndReturn(response, BuildConfigurationPage.class);

        return wrapper.map(w -> w.getContent()).orElse(Collections.emptyList());
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations(int repositoryId, String scmRevision)
            throws PNCRequestException {
        String query = String.format("(repositoryConfiguration=='%d';scmRevision=='%s')",
                repositoryId, scmRevision);
        return getBuildConfigurations(query);
    }

    @Override
    public List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws PNCRequestException {
        return Collections.emptyList();
    }

    @Override
    public Optional<BuildConfiguration> getBuildConfiguration(String name)
            throws PNCRequestException {
        String query = String.format("name=='%s'", name);
        return single(getBuildConfigurations(query), "build configuration for name %s", name);
    }

    @Override
    public Optional<BPMTask> getBPMTask(int taskId) throws PNCRequestException {
        BpmEndpoint endpoint = getEndpoint(BpmEndpoint.class);
        Response response = endpoint.getBPMTaskById(taskId);
        Optional<BPMTaskSingleton> wrapper = checkAndReturn(response, BPMTaskSingleton.class);
        return wrapper.map(w -> w.getContent());
    }

    @Override
    public Future<Integer> createRepositoryConfiguration(String url) {
        return CompletableFuture.supplyAsync(() -> createRepository(url));
        
    }

    private Integer createRepository(String url){
        try {
            BpmEndpoint endpoint = getEndpoint(BpmEndpoint.class);
            final Response response = endpoint.startRCCreationTask(getAccessToken(), new RepositoryConfigurationBPMCreate(url));
            if(response.getStatus() == SC_CONFLICT){
                return response.readEntity(RepositoryConfigurationConflict.class)
                        .getRepositoryConfigurationId();
            }
            Integer taskId = checkAndReturn(response,Integer.class)
                    .orElseThrow(() -> new PNCRequestException("Repository creation didn't return task id."));

            int tries = 13;
            while (tries-- > 0) {
                Optional<BPMTask> task = getBPMTask(taskId);
                if (task.isPresent()) {
                    BPMTask t = task.get();
                    for (BpmNotification e : t.getEvents()) {
                        switch (e.getEventType()) {
                            case "RC_REPO_CREATION_ERROR":
                            case "RC_REPO_CLONE_ERROR":
                            case "RC_CREATION_ERROR":
                                throw new PNCRequestException("Repository creation failed: "
                                        + e.getEventType());
                            case "RC_CREATION_SUCCESS": {
                                return (Integer) e.getData().get("repositoryId");
                            }
                            default:
                                break;
                        }
                    }
                }
                try {
                    Thread.sleep(1000 * 30);
                } catch (InterruptedException ex) {
                    throw new PNCRequestException("Waiting for buildconfiguration " + url
                            + " was interrupted", ex);
                }
            }
            throw new PNCRequestException("Timeout while waiting for buildconfiguration " + url);
        } catch (PNCRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc)
            throws PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);
        Response response = endpoint.createNew(getAccessToken(), bc);
        return checkAndReturn(response, BuildConfigurationSingleton.class)
                .map(s -> s.getContent())
                .orElseThrow(() -> new PNCRequestException("PNC didn't return created object"));
    }

    @Override
    public void updateBuildConfiguration(BuildConfiguration bc) throws PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);
        checkCode(endpoint.update(getAccessToken(), bc.getId(), bc));
    }

    @Override
    public void deleteBuildConfiguration(BuildConfiguration bc) throws PNCRequestException {
        deleteBuildConfiguration(bc.getId());
    }

    @Override
    public void deleteBuildConfiguration(int bcId) throws PNCRequestException {
        BuildConfigurationEndpoint endpoint = getEndpoint(BuildConfigurationEndpoint.class);
        checkCode(endpoint.deleteSpecific(getAccessToken(), bcId));
    }

    @Override
    public BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws PNCRequestException {
        BuildConfigurationSetEndpoint endpoint = getEndpoint(BuildConfigurationSetEndpoint.class);
        Response response = endpoint.createNew(getAccessToken(), bcs);
        return checkAndReturn(response, BuildConfigurationSetSingleton.class)
                .map(s -> s.getContent())
                .orElseThrow(() -> new PNCRequestException("PNC didn't return created object"));
    }

    @Override
    public Optional<BuildConfigurationSet> findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds) throws PNCRequestException {
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
            throws PNCRequestException {
        String query = String.format("product.id==%s;version=='%s'", productId, version);

        ProductVersionEndpoint endpoint = getEndpoint(ProductVersionEndpoint.class);
        Response response = endpoint.getAll(0, 2, "", query);
        List<ProductVersion> pvs = checkAndReturn(response, ProductVersionPage.class)
                .map(s -> s.getContent())
                .orElse(Collections.emptyList());

        return single(pvs, "product version for product %d and version %s", productId, version);
    }

    @Override
    public ProductVersion createProductVersion(ProductVersion pv) throws PNCRequestException {
        ProductVersionEndpoint endpoint = getEndpoint(ProductVersionEndpoint.class);
        Response response = endpoint.createNew(getAccessToken(), pv);
        return checkAndReturn(response, ProductVersionSingleton.class)
                .map(s -> s.getContent())
                .orElseThrow(() -> new PNCRequestException("PNC didn't return created object"));
    }

    private <T> Optional<T> checkAndReturn(Response response, Class<T> type)
            throws PNCRequestException {
        try {
            switch (response.getStatus()) {
                case SC_OK:
                case SC_CREATED:
                    return Optional.of(response.readEntity(type));
                case SC_NO_CONTENT:
                    return Optional.empty();
                case SC_UNAUTHORIZED:
                case SC_FORBIDDEN:
                    throw new IllegalStateException("Current user not authenticated.");
                default:
                    throw new PNCRequestException("Error when communicating with PNC, got reply "
                            + response.getStatusInfo() + " " + response.readEntity(String.class));
            }
        } finally {
            response.close();
        }
    }

    private void checkCode(Response response) throws PNCRequestException {
        try {
            switch (response.getStatus()) {
                case SC_OK:
                case SC_CREATED:
                case SC_NO_CONTENT:
                    return;
                case SC_UNAUTHORIZED:
                case SC_FORBIDDEN:
                    throw new IllegalStateException("Current user not authenticated.");
                default:
                    throw new PNCRequestException("Error when communicating with PNC, got reply "
                            + response.getStatusInfo() + " " + response.readEntity(String.class));
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
