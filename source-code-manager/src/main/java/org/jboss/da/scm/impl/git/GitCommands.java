package org.jboss.da.scm.impl.git;

import static org.jboss.da.scm.impl.git.GitUtils.DEFAULT_REMOTE_NAME;
import static org.jboss.da.scm.impl.git.GitUtils.FETCH_HEAD;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FileUtils;
import org.jboss.pnc.common.log.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.common.process.AbstractExecutionException;
import io.smallrye.common.process.ProcessBuilder;

/**
 * Composes the git commands (using {@link GitUtils}) and executes them as shell processes.
 */
@ApplicationScoped
public class GitCommands {

    private static final Logger logger = LoggerFactory.getLogger(GitCommands.class);

    private static final int MAX_LINE_LENGTH = 8192;

    /**
     * Clones the repository at the given revision into the given directory.
     * <p>
     * Fetching the requested revision shallowly for branches, tags, and commit hashes alike.
     * Is shallow clone fails, it falls back to a full clone followed by a checkout.
     *
     * @param url URL of the repository to clone
     * @param revision revision to clone, the default branch is cloned when null or empty
     * @param cloneTo directory to clone the repository into
     * @throws GitException thrown if the repository could not be cloned
     */
    public void cloneRepository(String url, String revision, File cloneTo) {
        if (revision == null || revision.isEmpty()) {
            if (!execute(cloneTo, GitUtils.clone(url, true))) {
                throw new GitException("Could not clone repository " + LogSanitizer.clean(url));
            }
            return;
        }

        if (shallowFetchRevision(url, revision, cloneTo)) {
            return;
        }

        logger.info(
                "Could not fetch revision {} of {} shallowly, falling back to a full clone",
                LogSanitizer.clean(revision),
                LogSanitizer.clean(url));
        cleanDirectory(cloneTo);

        if (!execute(cloneTo, GitUtils.clone(url, false)) || !execute(cloneTo, GitUtils.checkout(revision))) {
            cleanDirectory(cloneTo);
            throw new GitException(
                    "Could not clone repository " + LogSanitizer.clean(url) + " at revision "
                            + LogSanitizer.clean(revision));
        }
    }

    private boolean shallowFetchRevision(String url, String revision, File cloneTo) {
        return execute(cloneTo, GitUtils.init())
                && execute(cloneTo, GitUtils.addRemote(DEFAULT_REMOTE_NAME, url))
                && execute(cloneTo, GitUtils.fetchRef(DEFAULT_REMOTE_NAME, revision, true))
                && execute(cloneTo, GitUtils.checkout(FETCH_HEAD));
    }

    /**
     * Runs the given command in the given working directory. Returns true if it exited successfully, false otherwise.
     */
    private boolean execute(File workingDir, List<String> command) {
        StringBuilder errorOutput = new StringBuilder();

        ProcessBuilder<Void> processBuilder = ProcessBuilder.newBuilder(command.get(0))
                .arguments(command.subList(1, command.size()))
                .directory(workingDir.toPath())
                // need to add those variables to tell git not to prompt us if repository does not exist
                .modifyEnvironment(env -> {
                    env.put("GIT_ASKPASS", "/bin/echo"); // git <= 2.3
                    env.put("GIT_TERMINAL_PROMPT", "0"); // git > 2.3
                });
        processBuilder.output().discard();
        processBuilder.error()
                .logOnSuccess(false)
                .consumeLinesWith(MAX_LINE_LENGTH, line -> errorOutput.append(line).append(System.lineSeparator()));

        try {
            processBuilder.run();
            return true;
        } catch (AbstractExecutionException ex) {
            logger.info(
                    "Command '{}' failed: {}",
                    String.join(" ", command),
                    errorOutput.toString());
            return false;
        }
    }

    private void cleanDirectory(File directory) {
        try {
            FileUtils.cleanDirectory(directory);
        } catch (IOException ex) {
            logger.warn("Could not clean directory {}", LogSanitizer.clean(directory.toString()), ex);
        }
    }
}
