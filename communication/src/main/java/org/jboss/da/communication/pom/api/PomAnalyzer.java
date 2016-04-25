package org.jboss.da.communication.pom.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface PomAnalyzer {

    Optional<MavenProject> readPom(File pomPath);

    Optional<MavenProject> readPom(InputStream is) throws CommunicationException;

    /**
     * Given the directory of a project, and the directory of the project which
     * we'll consider as the root project, return the GAVDependencyTree of the
     * root project.
     *
     * @param pomRepoDir Directory of the project to analyze
     * @param pomPath Directory of the root project to analyze
     * @param repositories Additional repositories to analyze
     * @return The GAVDependencyTree of the root project
     * @throws PomAnalysisException
     */
    GAVDependencyTree readRelationships(File pomRepoDir, String pomPath, List<String> repositories)
            throws PomAnalysisException;

    /**
     * Given the directory of a project, and the gav of the project which
     * we'll consider as the root project, return the GAVDependencyTree of the
     * root project.
     *
     * @param pomRepoDir Directory of the project to analyze
     * @param gav GAV of the root project to analyze
     * @return The GAVDependencyTree of the root project
     * @throws PomAnalysisException
     */
    GAVDependencyTree readRelationships(File pomRepoDir, GAV gav) throws PomAnalysisException;

    /**
     * Given the directory of a project, and the gav of the project which
     * we'll consider as the root project, return the GAVDependencyTree of the
     * root project.
     *
     * @param pomRepoDir Directory of the project to analyze
     * @param gav GAV of the root project to analyze
     * @return The GAVDependencyTree of the root project
     * @throws PomAnalysisException
     */
    Set<GAV> getToplevelDepency(File pomRepoDir, GAV gav) throws PomAnalysisException;

    public Optional<File> getPOMFileForGAV(File tempDir, GAV gav);

    public Map<GA, Set<GAV>> getDependenciesOfModules(File scmDir, String pomPath,
            List<String> repositories) throws PomAnalysisException;

    public MavenPomView getGitPomView(File repoDir, String pomPath, List<String> repositories)
            throws PomAnalysisException;

    public MavenPomView getMavenPomView(InputStream is) throws ConfigurationParseException,
            GalleyMavenException;
}
