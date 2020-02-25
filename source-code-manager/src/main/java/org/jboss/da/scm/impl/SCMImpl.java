package org.jboss.da.scm.impl;

import org.apache.maven.scm.ScmException;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SCMImpl implements SCM {

    @Inject
    Logger log;

    @Inject
    ScmFacade scm;

    @Inject
    SCMCache cache;

    @Override
    public File cloneRepository(SCMType scmType, String scmUrl, String revision) throws ScmException {
        SCMSpecifier spec = new SCMSpecifier(scmType, scmUrl, revision);

        FutureReference fref = cache.get(spec);

        if (fref.shouldIComplete()) {
            try {
                File tempDir = Files.createTempDirectory("cloned_repo").toFile();
                log.info("Cached repository for {} not found. Cloning to {}.", spec, tempDir);
                try {
                    scm.shallowCloneRepository(scmType, scmUrl, revision, tempDir);
                    DirectoryReference ref = new DirectoryReference(tempDir);
                    fref.complete(ref);
                    return tempDir;
                } catch (ScmException ex) {
                    fref.completeExceptionally(ex);
                    try {
                        FileUtils.deleteDirectory(tempDir);
                    } catch (IOException ioex) {

                        log.warn("Temporary directory could not be deleted", ioex);
                    }
                    throw ex;
                }
            } catch (IOException ex) {
                throw new ScmException("Could not create temp directory for cloning the repository", ex);
            }

        } else {
            try {
                File dir = fref.get(30, TimeUnit.MINUTES)
                        .get()
                        .orElseThrow(() -> new IllegalStateException("Now completed reference has empty file."));
                log.info("Cached repository for {} found in {}.", spec, dir);
                return dir;
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new ScmException("Could not obtain cloned repository.", ex);
            }
        }
    }
}
