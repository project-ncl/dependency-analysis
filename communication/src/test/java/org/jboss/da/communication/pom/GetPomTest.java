package org.jboss.da.communication.pom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */

@RunWith(MockitoJUnitRunner.class)
public class GetPomTest {

    @Spy
    private PomReader pomReader;

    @InjectMocks
    private PomAnalyzer pomAnalyzer = new PomAnalyzerImpl();

    private final static String GROUP_ID = "org.jboss.da";

    private final static String VERSION = "0.4.0-SNAPSHOT";

    private final static String ARTIFACT_ID_1 = "parent";

    private final static String ARTIFACT_ID_2 = "child";

    private final static String NAME = "dependency-analyzer";

    private final static String SCM_URL = "https://github.com/project-ncl/dependency-analysis";

    private final static String SCM_REVISION = "0.4";

    @Test
    public void testGetPom1() throws CommunicationException, FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource("pom/test1.xml").getFile());
        MavenProject pom;

        pom = pomAnalyzer.readPom(file).get();
        assertPom(pom, ARTIFACT_ID_1);
        assertSCM(pom);

        pom = pomAnalyzer.readPom(new FileInputStream(file)).get();
        assertPom(pom, ARTIFACT_ID_1);
        assertSCM(pom);
    }

    @Test
    public void testGetPom2() throws CommunicationException, FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource("pom/test2.xml").getFile());
        MavenProject pom;

        pom = pomAnalyzer.readPom(file).get();
        assertPom(pom, ARTIFACT_ID_2);
    }

    private void assertPom(MavenProject pom, String artifactId) {
        assertEquals(GROUP_ID, pom.getGroupId());
        assertEquals(artifactId, pom.getArtifactId());
        assertEquals(VERSION, pom.getVersion());
        assertEquals(NAME, pom.getName());
    }

    private void assertSCM(MavenProject pom) {
        assertNotNull(pom.getScm());
        assertEquals(SCM_URL, pom.getScm().getUrl());
        assertEquals(SCM_REVISION, pom.getScm().getTag());
    }

}
