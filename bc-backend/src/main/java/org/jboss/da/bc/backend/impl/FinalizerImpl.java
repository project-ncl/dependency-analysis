package org.jboss.da.bc.backend.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.da.bc.backend.api.BCSetGenerator;
import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnectorProvider;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.slf4j.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class FinalizerImpl implements Finalizer {

    private static final String NO_SCM_URL = "Build configuration selected for creation, but doesn't have scm information.";

    @Inject
    private Logger log;

    @Inject
    private PNCConnectorProvider pnc;

    @Inject
    private BCSetGenerator bcSetGenerator;

    private String token;

    @Override
    public Integer createBCs(int productId, String productVersion, ProjectHiearchy toplevelBc,
            String bcSetName, String authToken) throws CommunicationException, PNCRequestException {
        this.token = authToken;
        Set<Integer> ids = new HashSet<>();
        HashMap<String, Future<Integer>> repos = new HashMap<>();
        try {
            createRepositories(toplevelBc, repos);
            createDeps(toplevelBc, ids, repos);
            int productVersionId = bcSetGenerator.createProductVersion(productId, productVersion,
                    token);
            bcSetGenerator.createBCSet(bcSetName, productVersionId, new ArrayList<>(ids), token);
            return productVersionId;
        } catch (CommunicationException | PNCRequestException | RuntimeException ex) {
            for (Integer id : ids) {
                try {
                    pnc.getAuthConnector(token).deleteBuildConfiguration(id);
                } catch (CommunicationException | PNCRequestException | RuntimeException e) {
                    log.error("Rollback: Failed to delete configuration " + id, e);
                }
            }
            throw new RuntimeException("Fail while finishing import process. Rolled back.", ex);
        }
    }

    @Override
    public Integer createBCs(ProjectHiearchy toplevelBc, String authToken)
            throws CommunicationException, PNCRequestException {
        this.token = authToken;
        Set<Integer> ids = new HashSet<>();
        HashMap<String, Future<Integer>> repos = new HashMap<>();
        try {
            createRepositories(toplevelBc, repos);
            Set<Integer> toplevelId = createDeps(toplevelBc, ids, repos);
            return toplevelId.iterator().next(); // get single integer
        } catch (CommunicationException | PNCRequestException | RuntimeException ex) {
            for (Integer id : ids) {
                try {
                    pnc.getAuthConnector(token).deleteBuildConfiguration(id);
                } catch (CommunicationException | PNCRequestException | RuntimeException e) {
                    log.error("Rollback: Failed to delete configuration " + id, e);
                }
            }
            throw new RuntimeException("Fail while finishing import process. Rolled back.", ex);
        }
    }

    /**
     * Creates Build configurations.
     * @return Set containing:
     *      a) single integer, when the hiearchy object is selected
     *      b) multiple integers, when the hiearchy object is not selected AND it has selected dependencies
     *      c) NO integer, when the hiearchy object is not selected AND it has NO selected dependencies
     */
    Set<Integer> createDeps(ProjectHiearchy hiearchy, Set<Integer> allDependencyIds, Map<String, Future<Integer>> repos) throws CommunicationException, PNCRequestException {
        Set<Integer> nextLevelDependencyIds = new HashSet<>();

        for (ProjectHiearchy dep : hiearchy.getDependencies()) {
            nextLevelDependencyIds.addAll(createDeps(dep, allDependencyIds, repos));
        }

        if(hiearchy.isSelected()){
            BuildConfiguration bc;
            ProjectDetail project = hiearchy.getProject();
            if (project.isUseExistingBc()) {
                List<BuildConfiguration> existingBcs = new ArrayList<>();

                Optional<ProjectDetail.SCM> scm = project.getSCM();
                if(scm.isPresent()){
                    existingBcs.addAll(pnc.getConnector().getBuildConfigurations(
                            scm.get().getUrl(),
                            scm.get().getRevision()));
                }

                Optional<BuildConfiguration> optionalBc = existingBcs.stream()
                        .filter(x -> project.getBcId().equals(x.getId()))
                        .findFirst();

                bc = optionalBc.orElseThrow(() -> new IllegalStateException("useExistingBC is true, but there is no BC to use."));
            } else {
                BuildConfigurationCreate bcc = toBC(project, repos);
                bc = pnc.getAuthConnector(token).createBuildConfiguration(bcc);
            }
            if (!nextLevelDependencyIds.isEmpty()) {
                bc.getDependencyIds().addAll(nextLevelDependencyIds);
                pnc.getAuthConnector(token).updateBuildConfiguration(bc);
            }

            allDependencyIds.add(bc.getId());
            return Collections.singleton(bc.getId());
        }else{
            return nextLevelDependencyIds;
        }
    }

    private BuildConfigurationCreate toBC(ProjectDetail project, Map<String, Future<Integer>> repos) throws CommunicationException {
        BuildConfigurationCreate bc = new BuildConfigurationCreate();
        bc.setBuildScript(project.getBuildScript());
        bc.setDescription(project.getDescription());
        bc.setEnvironmentId(project.getEnvironmentId());
        bc.setName(project.getName());
        bc.setProjectId(project.getProjectId());
        ProjectDetail.SCM scmInfo = project.getSCM().orElseThrow(() -> new IllegalArgumentException(
                NO_SCM_URL));

        Future<Integer> repo = repos.get(scmInfo.getUrl());
        
        try {
            bc.setRepositoryId(repo.get());
        } catch (InterruptedException | ExecutionException ex) {
            throw new CommunicationException("Failed to wait for repository creation", ex);
        }
        bc.setScmRevision(scmInfo.getRevision());

        return bc;
    }

    private void createRepositories(ProjectHiearchy toplevelBc, HashMap<String, Future<Integer>> repos) throws CommunicationException {
        for(ProjectHiearchy d: toplevelBc.getDependencies()){
            createRepositories(d, repos);
        }
        
        if(toplevelBc.isSelected()){
            ProjectDetail project = toplevelBc.getProject();
            if(!project.isUseExistingBc()){
                Optional<ProjectDetail.SCM> scm = project.getSCM();
                ProjectDetail.SCM scmInfo = scm.orElseThrow(() -> new IllegalArgumentException(
                        NO_SCM_URL));
                if(!repos.containsKey(scmInfo.getUrl())){
                    Future<Integer> rcid = pnc.getAuthConnector(token)
                            .createRepositoryConfiguration(scmInfo.getUrl());
                    repos.put(scmInfo.getUrl(), rcid);
                }
            }
        }
    }
}
