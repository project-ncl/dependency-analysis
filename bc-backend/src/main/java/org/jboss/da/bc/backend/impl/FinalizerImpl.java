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
import org.jboss.da.communication.pnc.model.BuildConfigurationBPMCreate;
import org.slf4j.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class FinalizerImpl implements Finalizer {

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
        try {
            create(toplevelBc);
            createDeps(toplevelBc, ids);
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
    public Integer createBCs(int id, ProjectHiearchy toplevelBc, String authToken)
            throws CommunicationException, PNCRequestException {
        this.token = authToken;
        Set<Integer> ids = new HashSet<>();
        create(toplevelBc);
        Set<Integer> toplevelId = createDeps(toplevelBc, ids);
        return toplevelId.iterator().next(); // get single integer
    }

    /**
     * Creates Build configurations.
     * @return Set containing:
     *      a) single integer, when the hiearchy object is selected
     *      b) multiple integers, when the hiearchy object is not selected AND it has selected dependencies
     *      c) NO integer, when the hiearchy object is not selected AND it has NO selected dependencies
     */
    Set<Integer> createDeps(ProjectHiearchy hiearchy, Set<Integer> allDependencyIds) throws CommunicationException, PNCRequestException {
        Set<Integer> nextLevelDependencyIds = new HashSet<>();

        for (ProjectHiearchy dep : hiearchy.getDependencies()) {
            nextLevelDependencyIds.addAll(createDeps(dep, allDependencyIds));
        }

        if(hiearchy.isSelected()){
            BuildConfiguration bc;
            ProjectDetail project = hiearchy.getProject();
            if (project.isUseExistingBc()) {
                List<BuildConfiguration> existingBcs = new ArrayList<>();

                Optional<ProjectDetail.SCM> internalSCM = project.getInternalSCM();
                if(internalSCM.isPresent()){
                    existingBcs.addAll(pnc.getConnector().getBuildConfigurations(
                            internalSCM.get().getUrl(),
                            internalSCM.get().getRevision()));
                }

                Optional<ProjectDetail.SCM> externalSCM = project.getExternalSCM();
                if(externalSCM.isPresent()){
                    existingBcs.addAll(pnc.getConnector().getBuildConfigurations(
                            externalSCM.get().getUrl(),
                            externalSCM.get().getRevision()));
                }

                Optional<BuildConfiguration> optionalBc = existingBcs.stream()
                        .filter(x -> project.getBcId().equals(x.getId()))
                        .findFirst();

                bc = optionalBc.orElseThrow(() -> new IllegalStateException("useExistingBC is true, but there is no BC to use."));
            } else {
                bc = waitForBC(project.getName());
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

    private BuildConfiguration waitForBC(String name) throws CommunicationException,
            PNCRequestException {
        int tries = 13;
        while (tries-- > 0) {
            Optional<BuildConfiguration> buildConfiguration = pnc.getConnector()
                    .getBuildConfiguration(name);
            if (buildConfiguration.isPresent()) {
                return buildConfiguration.get();
            }

            try {
                Thread.sleep(1000 * 30);
            } catch (InterruptedException ex) {
                throw new CommunicationException("Waiting for buildconfiguration " + name
                        + " was interrupted.", ex);
            }
        }
        throw new CommunicationException("Timeout while waiting for buildconfiguration " + name
                + ".");
    }

    private void create(ProjectHiearchy hiearchy) throws CommunicationException,
            PNCRequestException {
        for (ProjectHiearchy dep : hiearchy.getDependencies()) {
            create(dep);
        }

        if (hiearchy.isSelected()) {
            ProjectDetail project = hiearchy.getProject();
            if (!project.isUseExistingBc()) {
                BuildConfigurationBPMCreate bcc = toBC(project);
                pnc.getAuthConnector(token).createBuildConfiguration(bcc);
            }
        }
    }

    private BuildConfigurationBPMCreate toBC(ProjectDetail project) {
        BuildConfigurationBPMCreate bc = new BuildConfigurationBPMCreate();
        bc.setBuildScript(project.getBuildScript());
        bc.setDescription(project.getDescription());
        bc.setEnvironmentId(project.getEnvironmentId());
        bc.setName(project.getName());
        bc.setProjectId(project.getProjectId());

        project.getInternalSCM().ifPresent(scm -> {
            bc.setScmRepoURL(scm.getUrl());
            bc.setScmRevision(scm.getRevision());
        });
        project.getExternalSCM().ifPresent(scm -> {
            bc.setScmExternalRepoURL(scm.getUrl());
            bc.setScmExternalRevision(scm.getRevision());
        });

        return bc;
    }
}
