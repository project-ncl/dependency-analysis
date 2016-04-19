package org.jboss.da.bc.impl;

import org.jboss.da.bc.backend.api.POMInfo;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.ProductBuildConfigurationGenerator;
import org.jboss.da.bc.api.ProjectBuildConfigurationGenerator;
import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.bc.model.DependencyAnalysisStatus;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.backend.ProductGeneratorEntity;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectGeneratorEntity;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.GAVToplevelDependencies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.da.bc.model.backend.GeneratorEntity;
import org.jboss.da.bc.model.backend.GeneratorEntity.EntityConstructor;
import org.jboss.da.model.rest.GAV;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ApplicationScoped
public class BuildConfigurationGeneratorImpl implements ProductBuildConfigurationGenerator,
        ProjectBuildConfigurationGenerator {

    private final Pattern bcNamePattern = Pattern
            .compile("^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?<!\\.git)$");

    @Inject
    private DependencyTreeGenerator depGenerator;

    @Inject
    private POMInfoGenerator pom;

    @Inject
    private Finalizer finalizer;

    @Inject
    private ProjectHiearchyCreator nextLevel;

    private <T extends GeneratorEntity> T prepareEntity(EntityConstructor<T> constructor,
            SCMLocator scm, int id) throws ScmException, PomAnalysisException {
        GAVToplevelDependencies deps = depGenerator.getToplevelDependencies(scm);

        Optional<POMInfo> pomInfo = pom.getPomInfo(scm.getScmUrl(), scm.getRevision(),
                scm.getPomPath());

        T entity = constructor.construct(scm, id, deps.getGav());

        entity.getToplevelProject().setDescription(
                ProjectHiearchyCreator.getDescription(pomInfo, deps.getGav()));

        entity.getToplevelProject().setName(ProjectHiearchyCreator.getName(deps.getGav()));

        ProjectHiearchy ph = entity.getToplevelBc();

        ph.setDependencies(nextLevel.processDependencies(ph, deps.getDependencies()));

        // the top level BC has its dependencies analyzed
        ph.setAnalysisStatus(DependencyAnalysisStatus.ANALYSED);

        return entity;
    }

    @Override
    public ProductGeneratorEntity startBCGeneration(SCMLocator scm, int productId,
            String productVersion) throws ScmException, PomAnalysisException {
        ProductGeneratorEntity entity = prepareEntity(
                ProductGeneratorEntity.getConstructor(productVersion), scm, productId);

        GAV gav = entity.getToplevelProject().getGav();
        entity.setBcSetName(gav.toString() + "-" + UUID.randomUUID().toString().substring(0, 5));

        return entity;
    }

    @Override
    public ProjectGeneratorEntity startBCGeneration(SCMLocator scm, int projectId)
            throws ScmException, PomAnalysisException {
        ProjectGeneratorEntity entity = prepareEntity(ProjectGeneratorEntity.getConstructor(), scm,
                projectId);

        return entity;
    }

    @Override
    public ProductGeneratorEntity iterateBCGeneration(ProductGeneratorEntity projects) {
        nextLevel.iterateNextLevel(projects.getToplevelBc());
        return projects;
    }

    @Override
    public ProjectGeneratorEntity iterateBCGeneration(ProjectGeneratorEntity projects) {
        nextLevel.iterateNextLevel(projects.getToplevelBc());
        return projects;
    }

    @Override
    public Optional<Integer> createBC(ProductGeneratorEntity projects)
            throws CommunicationException, PNCRequestException {
        if (StringUtils.isBlank(projects.getBcSetName()))
            throw new IllegalStateException("BCSet name is blank.");
        if (StringUtils.isBlank(projects.getProductVersion()))
            throw new IllegalStateException("Product version is blank.");

        if (!validate(projects.getToplevelBc()))
            return Optional.empty();

        return Optional.of(finalizer.createBCs(projects.getId(), projects.getProductVersion(),
                projects.getToplevelBc(), projects.getBcSetName()));
    }

    @Override
    public Optional<Integer> createBC(ProjectGeneratorEntity projects)
            throws CommunicationException, PNCRequestException {

        if (!validate(projects.getToplevelBc()))
            return Optional.empty();

        return Optional.of(finalizer.createBCs(projects.getId(), projects.getToplevelBc()));
    }

    private boolean validate(ProjectHiearchy hiearchy) throws IllegalStateException {
        boolean noerror = true;

        if (hiearchy.isSelected()) {
            ProjectDetail project = hiearchy.getProject();

            if (project.isUseExistingBc() && !project.isBcExists()) {
                project.addError(BcError.NO_EXISTING_BC);
                project.setUseExistingBc(false);
                noerror = false;
            }

            if (!project.isUseExistingBc() && project.getEnvironmentId() == null) {
                project.addError(BcError.NO_ENV_SELECTED);
                noerror = false;
            }

            if (!project.isUseExistingBc() && project.getProjectId() == null) {
                project.addError(BcError.NO_PROJECT_SELECTED);
                noerror = false;
            }

            Matcher m = bcNamePattern.matcher(project.getName());
            if (!m.matches()) {
                project.addError(BcError.NO_NAME);
                noerror = false;
            }
        }

        for (ProjectHiearchy dep : hiearchy.getDependencies()) {
            noerror &= validate(dep);
        }
        return noerror;
    }

}
