package org.jboss.da.communication.pom.api;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.model.MavenProject;

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
     * @return The GAVDependencyTree of the root project
     * @throws PomAnalysisException
     */
    GAVDependencyTree readRelationships(File pomRepoDir, File pomPath) throws PomAnalysisException;

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

    public Optional<File> getPOMFileForGAV(File tempDir, GAV gav);

}
