package org.jboss.da.bc.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.backend.api.BcChecker;
import org.jboss.da.bc.backend.api.POMInfo;
import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.bc.model.DependencyAnalysisStatus;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.GAVToplevelDependencies;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.slf4j.Logger;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@Stateless
public class ProjectHiearchyCreator {

    @Inject
    private Logger log;

    @Inject
    private DependencyTreeGenerator depGenerator;

    @Inject
    private POMInfoGenerator pom;

    @Inject
    private SCMConnector scm;

    @Inject
    private BcChecker bcFinder;

    @Inject
    private VersionFinder versionFinder;

    private ProjectHiearchy toplevel;

    private String scmUrl;

    private String revison;

    public void iterateNextLevel(ProjectHiearchy toplevel) {
        this.toplevel = toplevel;
        this.scmUrl = toplevel.getProject().getScmUrl();
        this.revison = toplevel.getProject().getScmRevision();

        iterate(toplevel);
    }

    public Set<ProjectHiearchy> processDependencies(ProjectHiearchy toplevel,
            Collection<GAV> dependencies) {
        this.toplevel = toplevel;
        this.scmUrl = toplevel.getProject().getScmUrl();
        this.revison = toplevel.getProject().getScmRevision();

        return toProjectHiearchies(dependencies);
    }

    /**
     * Iterate and fill next level dependencies where selected
     */
    private void iterate(ProjectHiearchy hiearchy) {
        DependencyAnalysisStatus status = hiearchy.getAnalysisStatus();
        if (DependencyAnalysisStatus.ANALYSED.equals(status) || !hiearchy.isSelected()) {
            // Dependencies already processed, search next level
            // or this dependency is not selected, but can have selected childs
            for (ProjectHiearchy dep : hiearchy.getDependencies()) {
                iterate(dep);
            }
        } else if (DependencyAnalysisStatus.NOT_ANALYSED.equals(status)
                && hiearchy.getDependencies().isEmpty()) {
            // Dependencies not yet processed, get and process them
            setDependencies(hiearchy);
        }
    }

    /**
     * Tries to find dependencies in AProx, if not found in AProx, try to found in the original SCM
     * repository.
     * @throws PomAnalysisException 
     * @throws ScmException 
     */
    private void setDependencies(ProjectHiearchy hiearchy) {
        GAV gav = hiearchy.getProject().getGav();
        GAVToplevelDependencies dependencies;
        try {
            dependencies = depGenerator.getToplevelDependencies(gav);
            hiearchy.setDependencies(toProjectHiearchies(dependencies));
            hiearchy.setAnalysisStatus(DependencyAnalysisStatus.ANALYSED);
        } catch (CommunicationException ex) {
            log.warn("Failed to get dependencies for " + gav, ex);
            hiearchy.getProject().addError(BcError.NO_DEPENDENCY);
            hiearchy.setAnalysisStatus(DependencyAnalysisStatus.FAILED);
        } catch (FindGAVDependencyException ex) {
            ProjectDetail project = toplevel.getProject();
            try {
                // try to get dependencies from scm url instead
                dependencies = depGenerator.getToplevelDependencies(project.getScmUrl(),
                        project.getScmRevision(), gav);
                hiearchy.setDependencies(toProjectHiearchies(dependencies));
                hiearchy.setAnalysisStatus(DependencyAnalysisStatus.ANALYSED);
            } catch (ScmException ex_scm) {
                hiearchy.getProject().addError(BcError.SCM_EXCEPTION);
                hiearchy.getProject().addError(BcError.NO_DEPENDENCY);
                hiearchy.setAnalysisStatus(DependencyAnalysisStatus.FAILED);
            } catch (PomAnalysisException ex_pom) {
                hiearchy.getProject().addError(BcError.POM_EXCEPTION);
                hiearchy.getProject().addError(BcError.NO_DEPENDENCY);
                hiearchy.setAnalysisStatus(DependencyAnalysisStatus.FAILED);
            }

        }
    }

    private Set<ProjectHiearchy> toProjectHiearchies(GAVToplevelDependencies deps) {
        return toProjectHiearchies(deps.getDependencies());
    }

    private Set<ProjectHiearchy> toProjectHiearchies(Collection<GAV> gavs) {
        return gavs.stream().map(dep -> toProjectHiearchy(dep)).collect(Collectors.toSet());
    }

    /**
     * Creates new ProjectHiearchy from GAV.
     */
    private ProjectHiearchy toProjectHiearchy(GAV gav) {
        ProjectDetail project = new ProjectDetail(gav);

        Optional<POMInfo> pomInfo = getPomInfo(gav);

        project.setName(getName(gav)); // name
        project.setDescription(getDescription(pomInfo, gav)); // description
        setSCMInfo(project, pomInfo); // scmUrl, useExistingBc
        findExistingBuildConfiguration(project); // bcExists, useExistingBc
        checkInternallyBuilt(project); // internallyBuilt

        return new ProjectHiearchy(project, false);
    }

    public static String getName(GAV gav) {
        return String.format("%s-%s-%s", gav.getArtifactId(), gav.getVersion(), UUID.randomUUID()
                .toString().substring(0, 5));
    }

    public static String getDescription(Optional<POMInfo> pomInfo, GAV gav) {
        Optional<String> name = pomInfo.flatMap(p -> p.getName());

        return name.map(n -> String.format("Build Configuration for %s - %s.", gav, n))
                .orElse(String.format("Build Configuration for %s.", gav));
    }

    private Optional<POMInfo> getPomInfo(GAV gav) {
        Optional<POMInfo> pomInfo = Optional.empty();

        try {
            pomInfo = pom.getPomInfo(gav);
        } catch (CommunicationException | PomAnalysisException ex) {
            log.warn("Failed to get pom for gav " + gav + " from AProx", ex);
        }

        if (!pomInfo.isPresent()) {
            try {
                pomInfo = pom.getPomInfo(scmUrl, revison, gav);
            } catch (ScmException ex) {
                log.warn("Failed to get pom for gav " + gav + " from product SCM repository", ex);
            }
        }

        return pomInfo;
    }

    /**
     * Sets scmUrl and scmRevision
     */
    private void setSCMInfo(ProjectDetail project, Optional<POMInfo> pomInfo) {
        Optional<String> url = pomInfo.flatMap(p -> p.getScmURL());
        Optional<String> rev = pomInfo.flatMap(p -> p.getScmRevision());

        if (!url.isPresent() || !rev.isPresent()) {
            try {
                boolean gavInRepository = scm.isGAVInRepository(scmUrl, revison, project.getGav());
                if (gavInRepository) {
                    if (url.isPresent()) {
                        if (url.get().equals(scmUrl)) {
                            rev = Optional.of(revison);
                        }
                    } else {
                        if (rev.isPresent()) {
                            if (rev.get().equals(revison)) {
                                url = Optional.of(scmUrl);
                            }
                        } else {
                            url = Optional.of(scmUrl);
                            rev = Optional.of(revison);
                        }
                    }
                }
            } catch (ScmException ex) {
                log.warn("Failed to check if GAV " + project.getGav() + " is in repository.", ex);
            }
        }

        project.setScmUrl(url.orElse(null));
        project.setScmRevision(rev.orElse(null));
    }

    /**
     * Sets bcExists and useExistingBc
     * @param project Project with SCM information set.
     */
    private void findExistingBuildConfiguration(ProjectDetail project) {
        if (project.getScmUrl() == null || project.getScmRevision() == null)
            return;

        try {
            Optional<BuildConfiguration> found = bcFinder.lookupBcByScm(project.getScmUrl(),
                    project.getScmRevision());
            project.setBcExists(found.isPresent());
            project.setUseExistingBc(found.isPresent());
            project.setBcId(found.isPresent() ? found.get().getId() : null);

        } catch (CommunicationException | PNCRequestException ex) {
            log.warn("Failed to lookup existing BC for " + project.getGav(), ex);
        }
    }

    /**
     * Sets internallyBuilt
     */
    private void checkInternallyBuilt(ProjectDetail project) {
        try {
            Optional<String> bestMatchVersionFor = versionFinder.getBestMatchVersionFor(project
                    .getGav());
            project.setInternallyBuilt(bestMatchVersionFor);
        } catch (CommunicationException ex) {
            log.warn("Could not obtain best match version for " + project.getGav(), ex);
            project.setInternallyBuilt(Optional.empty());
        }
    }
}
