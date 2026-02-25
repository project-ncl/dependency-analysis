package org.jboss.da.communication.pom;

import org.jboss.da.communication.pom.model.MavenProject;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PomReaderTest {

    @Test
    public void testAnalyze() {
        PomReader reader = new PomReader();
        File file = new File(getClass().getClassLoader().getResource("pom/test1.xml").getFile());
        Optional<MavenProject> project = reader.analyze(file);
        assertTrue(project.isPresent());
        assertFalse(project.get().getMavenRepositories().isEmpty());

        assertTrue(
                project.get()
                        .getMavenRepositories()
                        .stream()
                        .anyMatch(
                                repo -> repo.getId().equals("eap")
                                        && repo.getUrl().equals("http://maven.repository.redhat.com/techpreview/all")));

        assertTrue(
                project.get()
                        .getMavenRepositories()
                        .stream()
                        .anyMatch(
                                repo -> repo.getId().equals("sonatype-snapshots") && repo.getUrl()
                                        .equals("http://oss.sonatype.org/content/repositories/snapshots")));

        assertEquals(2, project.get().getMavenRepositories().size());

    }
}
