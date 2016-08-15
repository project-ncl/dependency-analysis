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
import org.jboss.da.bc.backend.api.BcChecker;
import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnector;
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
    private PNCConnector pnc;

    @Inject
    private BCSetGenerator bcSetGenerator;

    @Inject
    private BcChecker bcFinder;

    @Override
    public Integer createBCs(int productId, String productVersion, ProjectHiearchy toplevelBc,
            String bcSetName) throws CommunicationException, PNCRequestException {
        Set<Integer> ids = new HashSet<>();
        try {
            create(toplevelBc, ids);
            int productVersionId = bcSetGenerator.createProductVersion(productId, productVersion);
            bcSetGenerator.createBCSet(bcSetName, productVersionId, new ArrayList<>(ids));
            return productVersionId;
        } catch (CommunicationException | PNCRequestException | RuntimeException ex) {
            for (Integer id : ids) {
                try {
                    pnc.deleteBuildConfiguration(id);
                } catch (CommunicationException | PNCRequestException | RuntimeException e) {
                    log.error("Rollback: Failed to delete configuration " + id, e);
                }
            }
            throw new RuntimeException("Fail while finishing import process. Rolled back.", ex);
        }
    }

    @Override
    public Integer createBCs(int id, ProjectHiearchy toplevelBc) throws CommunicationException,
            PNCRequestException {
        Set<Integer> ids = new HashSet<>();
        create(toplevelBc, ids);
        return create(toplevelBc, ids).iterator().next(); // get single integer
    }

    /**
     * Creates Build configurations.
     * @return Set containing:
     *      a) single integer, when the hiearchy object is selected
     *      b) multiple integers, when the hiearchy object is not selected AND it has selected dependencies
     *      c) NO integer, when the hiearchy object is not selected AND it has NO selected dependencies
     */
    Set<Integer> create(ProjectHiearchy hiearchy, Set<Integer> allDependencyIds) throws CommunicationException, PNCRequestException {
        Set<Integer> nextLevelDependencyIds = new HashSet<>();

        for (ProjectHiearchy dep : hiearchy.getDependencies()) {
            nextLevelDependencyIds.addAll(create(dep, allDependencyIds));
        }

        if(hiearchy.isSelected()){
            BuildConfiguration bc;
            ProjectDetail project = hiearchy.getProject();
            if (project.isUseExistingBc()) {
                List<BuildConfiguration> existingBcs = bcFinder.lookupBcByScm(project.getScmUrl(), project.getScmRevision());
                Optional<BuildConfiguration> optionalBc = existingBcs.stream()
                        .filter(x -> project.getBcId().equals(x.getId()))
                        .findFirst();
                
                bc = optionalBc.orElseThrow(() -> new IllegalStateException("useExistingBC is true, but there is no BC to use."));
            } else {

                BuildConfigurationBPMCreate bcc = toBC(project, nextLevelDependencyIds);

                bc = pnc.createBuildConfiguration(bcc);
            }

            allDependencyIds.add(bc.getId());
            return Collections.singleton(bc.getId());
        }else{
            return nextLevelDependencyIds;
        }
    }

    private BuildConfigurationBPMCreate toBC(ProjectDetail project, Set<Integer> deps) {
        BuildConfigurationBPMCreate bc = new BuildConfigurationBPMCreate();
        bc.setBuildScript(project.getBuildScript());
        bc.setDependencyIds(new ArrayList<>(deps));
        bc.setDescription(project.getDescription());
        bc.setEnvironmentId(project.getEnvironmentId());
        bc.setName(project.getName());
        bc.setProjectId(project.getProjectId());
        bc.setSCMLocation(project.getScmUrl(), project.getScmRevision());
        return bc;
    }
}
