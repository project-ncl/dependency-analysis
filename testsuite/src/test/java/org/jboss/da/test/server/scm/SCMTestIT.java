package org.jboss.da.test.server.scm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmException;
import org.jboss.da.scm.api.SCMType;
import org.jboss.da.scm.impl.ScmFacade;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class SCMTestIT {

    private void testDaGitClone(String scmUrl) throws Exception {
        Path tempDir = Files.createTempDirectory("da_temp_git_clone");
        ScmFacade scm = new ScmFacade();

        try {
            // the git commit is actually the one for tag 0.2.0
            scm.shallowCloneRepository(SCMType.GIT, scmUrl, "05ea9e1", tempDir.toFile());

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

    @Test
    public void shouldCloneGitRepository_Git() throws Exception {
        testDaGitClone("git://github.com/project-ncl/dependency-analysis.git");
    }

    @Test
    public void shouldFailClone_Ssh() throws Exception {
        // See JIRA NCL-1921 - ssh clone requires private key installation

        try {
            testDaGitClone("git@github.com:project-ncl/dependency-analysis.git");
        } catch (ScmException e) {
            // Verify the stack trace is indeed the expected private key failure and not something else
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String trace = sw.toString(); // stack trace as a string

            assertTrue(trace.contains("JGit checkout failure!"));
        }
    }

    @Test
    public void shouldCloneGitRepository_Https() throws Exception {
        testDaGitClone("https://github.com/project-ncl/dependency-analysis.git");
    }

    @Test
    public void shouldBeAbleToCloneGitTag() throws Exception {

        // this test should use the shallow cloning feature
        Path tempDir = Files.createTempDirectory("da_temp_git_clone");
        ScmFacade scm = new ScmFacade();

        try {
            scm.shallowCloneRepository(
                    SCMType.GIT,
                    "https://github.com/project-ncl/dependency-analysis.git",
                    "0.4.2",
                    tempDir.toFile());

            // make sure we shallow cloned
            ProcessBuilder pb = new ProcessBuilder("git", "rev-list", "HEAD", "--count");
            pb.directory(tempDir.toFile());
            Process p = pb.start();
            int status = p.waitFor();
            assertEquals(0, status);

            InputStreamReader inputStreamReader = new InputStreamReader(p.getInputStream());

            try (BufferedReader in = new BufferedReader(inputStreamReader)) {
                assertEquals("1", in.readLine());
            }

        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    @Ignore
    @Test
    public void shouldCloneSvnRepository() throws Exception {
        Path tempDir = Files.createTempDirectory("da_temp_svn_checkout");
        ScmFacade scm = new ScmFacade();

        try {
            // revision makes no sense for SVN
            scm.shallowCloneRepository(
                    SCMType.SVN,
                    "http://svn.apache.org/repos/asf/commons/proper/cli/tags/cli-1.3.1/",
                    "",
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
