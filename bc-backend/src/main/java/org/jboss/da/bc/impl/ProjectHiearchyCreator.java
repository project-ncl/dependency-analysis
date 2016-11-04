package org.jboss.da.bc.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.backend.api.POMInfo;
import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.DependencyAnalysisStatus;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;

import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.pnc.api.PNCConnectorProvider;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.impl.PNCConnectorProviderImpl;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.scm.api.SCMConnector;

import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.GAVToplevelDependencies;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.slf4j.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
    private PNCConnectorProvider pnc;

    @Inject
    private VersionFinder versionFinder;

    private ProjectHiearchy toplevel;

    private String scmUrl;

    private String revision;

    private String externalScmUrl;

    private String externalRevison;

    public void iterateNextLevel(ProjectHiearchy toplevel) {
        this.toplevel = toplevel;
        this.scmUrl = toplevel.getProject().getScmUrl();
        this.revision = toplevel.getProject().getScmRevision();
        this.externalScmUrl = toplevel.getProject().getExternalScmUrl();
        this.externalRevison = toplevel.getProject().getExternalScmRevision();

        iterate(toplevel);
    }

    public Set<ProjectHiearchy> processDependencies(ProjectHiearchy toplevel,
            Collection<GAV> dependencies) {
        this.toplevel = toplevel;
        this.scmUrl = toplevel.getProject().getScmUrl();
        this.revision = toplevel.getProject().getScmRevision();
        this.externalScmUrl = toplevel.getProject().getExternalScmUrl();
        this.externalRevison = toplevel.getProject().getExternalScmRevision();

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
                // try to get dependencies from internal and external scm url instead
                // intermal scm url and revision are prioritized
                if (project.getScmUrl() == null) {
                    dependencies = depGenerator.getToplevelDependencies(
                            project.getExternalScmUrl(), project.getExternalScmRevision(), gav);
                } else {
                    dependencies = depGenerator.getToplevelDependencies(project.getScmUrl(),
                            project.getScmRevision(), gav);
                }
                hiearchy.setDependencies(toProjectHiearchies(dependencies));
                hiearchy.setAnalysisStatus(DependencyAnalysisStatus.ANALYSED);
            } catch (ScmException ex_scm) {
                log.error("Failed while getting SCM repo", ex_scm);
                hiearchy.getProject().addError(BcError.SCM_EXCEPTION);
                hiearchy.getProject().addError(BcError.NO_DEPENDENCY);
                hiearchy.setAnalysisStatus(DependencyAnalysisStatus.FAILED);
            } catch (PomAnalysisException ex_pom) {
                log.error("Failed to analyse pom", ex_pom);
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
        getBuiltVersions(project); // internallyBuilt, availableVersions

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
                if (scmUrl == null) {
                    pomInfo = pom.getPomInfo(externalScmUrl, externalRevison, gav);
                } else {
                    pomInfo = pom.getPomInfo(scmUrl, revision, gav);
                }
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
        try {
            boolean gavInRepository = scm.isGAVInRepository(scmUrl, revision, project.getGav());
            if (gavInRepository) {
                //we should copy the coordinates (internal and external) from the toplevel
                project.setScmUrl(scmUrl);
                project.setScmRevision(revision);
                project.setExternalScmUrl(externalScmUrl);
                project.setExternalScmRevision(externalRevison);
            }
            else {
                Optional<String> url = pomInfo.flatMap(p -> p.getScmURL());
                Optional<String> rev = pomInfo.flatMap(p -> p.getScmRevision());
                //we should consider the url from pom to be external url
                //if the gav is not in repository and there are no coordinates in the pom
                //both internal and external url are null
                project.setExternalScmUrl(url.orElse(null));
                project.setExternalScmRevision(rev.orElse(null));
            }

        } catch (ScmException ex) {
            log.warn("Failed to check if GAV " + project.getGav() + " is in repository.", ex);

        }
    }

    /**
     * Sets bcExists and useExistingBc
     * @param project Project with SCM information set.
     */
    private void findExistingBuildConfiguration(ProjectDetail project) {
        if ((project.getScmUrl() == null || project.getScmRevision() == null)
                && (project.getExternalScmUrl() == null || project.getExternalScmRevision() == null))
            return;

        try {
            List<Integer> existingBcIds;
            if (project.getScmUrl() == null) {
                existingBcIds = pnc.getConnector().getBuildConfigurations(
                        project.getExternalScmUrl(), project.getExternalScmRevision()).stream()
                        .map(x -> x.getId()).collect(Collectors.toList());
            } else {
                existingBcIds = pnc.getConnector().getBuildConfigurations(project.getScmUrl(),
                        project.getScmRevision()).stream()
                        .map(x -> x.getId()).collect(Collectors.toList());
            }
            project.setExistingBCs(existingBcIds);
            project.setBcId(null);

        } catch (CommunicationException | PNCRequestException ex) {
            log.warn("Failed to lookup existing BC for " + project.getGav(), ex);
        }
    }

    /**
     * Looks up built versions for the ProjectDetail. Sets internallyBuilt, availableVersions.
     */
    private void getBuiltVersions(ProjectDetail project) {
        try {
            VersionLookupResult versionLookup = versionFinder.lookupBuiltVersions(project.getGav());
            List<String> versionsBuilt = versionLookup.getAvailableVersions();
            Optional<String> bestMatchVersionFor = versionLookup.getBestMatchVersion();

            project.setAvailableVersions(versionsBuilt);
            project.setInternallyBuilt(bestMatchVersionFor);
        } catch (CommunicationException ex) {
            log.warn("Could not obtain built versions for " + project.getGav(), ex);
            project.setInternallyBuilt(Optional.empty());
        }
    }
}
