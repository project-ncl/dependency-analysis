package org.jboss.da.bc.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.BuildConfigurationGenerator;
import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.backend.api.POMInfo;
import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.GAVToplevelDependencies;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
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
                Optional.of(nextLevel.processDependencies(ge, deps.getDependencies())));

        return ge;
    }

    @Override
    public GeneratorEntity iterateBCGeneration(GeneratorEntity projects) {
        return nextLevel.iterateNextLevel(projects);
    }

    @Override
    public Integer createBC(GeneratorEntity projects) throws Exception {
        if (StringUtils.isBlank(projects.getBcSetName()))
            throw new IllegalStateException("BCSet name is blank.");
        if (StringUtils.isBlank(projects.getName()))
            throw new IllegalStateException("Product name is blank.");
        if (StringUtils.isBlank(projects.getProductVersion()))
            throw new IllegalStateException("Product version is blank.");

        validate(projects.getToplevelBc());

        return finalizer.createBCs(projects.getName(), projects.getProductVersion(),
                projects.getToplevelBc(), projects.getBcSetName());
    }

    private void validate(ProjectHiearchy hiearchy) throws IllegalStateException {
        if (!hiearchy.isSelected())
            return;

        ProjectDetail project = hiearchy.getProject();

        if (project.isUseExistingBc() && !project.isBcExists())
            throw new IllegalStateException(
                    "Use existing build configuration is checked, but apperently there is not existing build configuration for "
                            + project.getGav());

        if (!project.isUseExistingBc() && project.getEnvironmentId() == null)
            throw new IllegalStateException("Environment id is null for " + project.getGav());

        if (!project.isUseExistingBc() && project.getProjectId() == null)
            throw new IllegalStateException("Project id is null for " + project.getGav());

        Matcher m = bcNamePattern.matcher(project.getName());
        if (!m.matches())
            throw new IllegalStateException(
                    "BuildConfiguration name doesn't match expected format. BuildConfiguration name: "
                            + project.getName());

        for (ProjectHiearchy dep : hiearchy.getDependencies().orElse(Collections.emptySet())) {
            validate(dep);
        }
    }
}
