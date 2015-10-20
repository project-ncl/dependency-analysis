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
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.scm.api.SCMConnector;
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

    private GeneratorEntity entity;

    private String scmUrl;

    private String revison;

    public GeneratorEntity iterateNextLevel(GeneratorEntity entity) {
        this.entity = entity;
        this.scmUrl = entity.getToplevelProject().getScmUrl();
        this.revison = entity.getToplevelProject().getScmRevision();

        iterate(entity.getToplevelBc());
        return entity;
    }

    public Set<ProjectHiearchy> processDependencies(GeneratorEntity entity,
            Collection<GAV> dependencies) {
        this.entity = entity;
        this.scmUrl = entity.getToplevelProject().getScmUrl();
        this.revison = entity.getToplevelProject().getScmRevision();

        return toProjectHiearchies(dependencies);
    }

    /**
     * Iterate and fill next level dependencies where selected
     */
    private void iterate(ProjectHiearchy hiearchy) {
        if (!hiearchy.isSelected())
            return; // Not selected, ignoring

        Optional<Set<ProjectHiearchy>> dependencies = hiearchy.getDependencies();
        if (!dependencies.isPresent())
            return; // Dependencies were already check with error, ignoring

        if (dependencies.get().isEmpty()) { // Dependencies not yet processed, get and process them
            GAV gav = hiearchy.getProject().getGav();
            Optional<GAVToplevelDependencies> deps = getDependencies(gav);
            hiearchy.setDependencies(toProjectHiearchies(deps));
        } else { // Dependencies already processed, search next level
            for (ProjectHiearchy dep : dependencies.get()) {
                iterate(dep);
            }
        }
    }

    /**
     * Tries to find dependencies in AProx, if not found in AProx, try to found in the original SCM
     * repository.
     */
    private Optional<GAVToplevelDependencies> getDependencies(GAV gav) {
        try {
            Optional<GAVToplevelDependencies> dependencies = depGenerator
                    .getToplevelDependencies(gav);
            if (!dependencies.isPresent()) {
                ProjectDetail project = entity.getToplevelProject();
                try {
                    dependencies = Optional.of(depGenerator.getToplevelDependencies(
                            project.getScmUrl(), project.getScmRevision(), gav));
                } catch (ScmException | PomAnalysisException ex) {
                    return Optional.empty();
                }
            }
            return dependencies;
        } catch (CommunicationException ex) {
            log.warn("Failed to get dependencies for " + gav, ex);
            return Optional.empty();
        }
    }

    private Optional<Set<ProjectHiearchy>> toProjectHiearchies(Optional<GAVToplevelDependencies> deps) {
        return deps.map(x -> toProjectHiearchies(x.getDependencies()));
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

        if(!url.isPresent() || !rev.isPresent()){
            try {
                boolean gavInRepository = scm.isGAVInRepository(scmUrl, revison, project.getGav());
                if(gavInRepository){
                    if(url.isPresent()){
                        if(url.get().equals(scmUrl)){
                            rev = Optional.of(revison);
                        }
                    }else{
                        if(rev.isPresent()){
                            if(rev.get().equals(revison)){
                                url = Optional.of(scmUrl);
                            }
                        }else{
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
        } catch (Exception ex) {
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
