package org.jboss.da.bc.impl;

import org.jboss.da.bc.backend.api.POMInfo;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.BuildConfigurationGenerator;
import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.bc.model.DependencyAnalysisStatus;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.GAVToplevelDependencies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ApplicationScoped
public class BuildConfigurationGeneratorImpl implements BuildConfigurationGenerator {

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

    @Override
    public GeneratorEntity startBCGeneration(SCMLocator scm, String productName,
            String productVersion) throws ScmException, PomAnalysisException {
        GAVToplevelDependencies deps = depGenerator.getToplevelDependencies(scm);
        Optional<POMInfo> pomInfo = pom.getPomInfo(scm.getScmUrl(), scm.getRevision(),
                scm.getPomPath());

        GeneratorEntity ge = new GeneratorEntity(scm, productName, deps.getGav(), productVersion);
        ge.setBcSetName(deps.getGav().toString() + "-"
                + UUID.randomUUID().toString().substring(0, 5));

        ge.getToplevelProject().setDescription(
                ProjectHiearchyCreator.getDescription(pomInfo, deps.getGav()));
        ge.getToplevelProject().setName(ProjectHiearchyCreator.getName(deps.getGav()));

        ge.getToplevelBc().setDependencies(
                nextLevel.processDependencies(ge, deps.getDependencies()));

        // the top level BC has its dependencies analyzed
        ge.getToplevelBc().setAnalysisStatus(DependencyAnalysisStatus.ANALYZED);

        return ge;
    }

    @Override
    public GeneratorEntity iterateBCGeneration(GeneratorEntity projects) {
        return nextLevel.iterateNextLevel(projects);
    }

    @Override
    public Optional<Integer> createBC(GeneratorEntity projects) throws CommunicationException,
            PNCRequestException {
        if (StringUtils.isBlank(projects.getBcSetName()))
            throw new IllegalStateException("BCSet name is blank.");
        if (StringUtils.isBlank(projects.getName()))
            throw new IllegalStateException("Product name is blank.");
        if (StringUtils.isBlank(projects.getProductVersion()))
            throw new IllegalStateException("Product version is blank.");

        if (!validate(projects.getToplevelBc()))
            return Optional.empty();

        return Optional.of(finalizer.createBCs(projects.getName(), projects.getProductVersion(),
                projects.getToplevelBc(), projects.getBcSetName()));
    }

    private boolean validate(ProjectHiearchy hiearchy) throws IllegalStateException {
        if (!hiearchy.isSelected())
            return true;

        boolean noerror = true;

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

        for (ProjectHiearchy dep : hiearchy.getDependencies()) {
            noerror &= validate(dep);
        }
        return noerror;
    }
}
