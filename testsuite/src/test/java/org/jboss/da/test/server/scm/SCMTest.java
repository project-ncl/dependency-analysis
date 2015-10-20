package org.jboss.da.test.server.scm;

import org.apache.commons.io.FileUtils;
import org.jboss.da.scm.impl.SCMClonner;
import org.jboss.da.scm.api.SCMType;
import org.junit.Ignore;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class SCMTest {

    @Test
    public void shouldCloneGitRepository() throws Exception {
        Path tempDir = Files.createTempDirectory("da_temp_git_clone");
        SCMClonner scm = new SCMClonner();

        try {
            // the git commit is actually the one for tag 0.2.0
            scm.cloneRepository(SCMType.GIT,
                    "https://github.com/project-ncl/dependency-analysis.git", "05ea9e1",
                    tempDir.toFile());

            Path pomPath = Paths.get(tempDir.toString(), "pom.xml");
            assertTrue(pomPath.toFile().exists());

            // check if the pom.xml we checkout has version 0.2.0
            boolean checkoutZeroTwoZeroVersionPom = false;

            Scanner scanner = new Scanner(pomPath.toFile());

            // old-school xml reading
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().equals("<version>0.2.0</version>")) {
                    checkoutZeroTwoZeroVersionPom = true;
                }
            }
            assertTrue(checkoutZeroTwoZeroVersionPom);
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    @Ignore
    @Test
    public void shouldCloneSvnRepository() throws Exception {
        Path tempDir = Files.createTempDirectory("da_temp_svn_checkout");
        SCMClonner scm = new SCMClonner();

        try {
            // revision makes no sense for SVN
            scm.cloneRepository(SCMType.SVN,
                    "http://svn.apache.org/repos/asf/commons/proper/cli/tags/cli-1.3.1/", "",
                    tempDir.toFile());

            Path pomPath = Paths.get(tempDir.toString(), "pom.xml");
            assertTrue(pomPath.toFile().exists());

            // check if the pom.xml we checkout has version 0.2.0
            boolean checkoutVersionOneThreeOne = false;

            Scanner scanner = new Scanner(pomPath.toFile());

            // old-school xml reading
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().equals("<version>1.3.1</version>")) {
                    checkoutVersionOneThreeOne = true;
                }
            }
            assertTrue(checkoutVersionOneThreeOne);
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }
}
