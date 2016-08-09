package org.jboss.da.scm.impl;

import org.jboss.da.scm.api.SCMType;
import org.jboss.da.scm.api.SCM;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmException;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@Singleton
public class SCMCache {

    @Inject
    Logger log;

    @Inject
    ScmFacade scm;

    private final Map<SCMSpecifier, DirectoryReference> cache = new HashMap<>();

    private final Map<SCMSpecifier, FutureReference> futureCache = new HashMap<>();

    public FutureReference get(SCMSpecifier spec){
        checkFutureCache();
        if(cache.containsKey(spec)){
            DirectoryReference ref = cache.get(spec);
            Optional<File> file = ref.get();
            if(file.isPresent()){
                return new FutureReference(ref);
            }
        }
        if(futureCache.containsKey(spec)){
            return new FutureReference(futureCache.get(spec));
        }
        return new FutureReference();
    }

    private void checkFutureCache() {
        Iterator<Map.Entry<SCMSpecifier, FutureReference>> it = futureCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<SCMSpecifier, FutureReference> e = it.next();
            FutureReference value = e.getValue();
            if (value.isDone()) {
                try {
                    cache.put(e.getKey(), value.get(1, TimeUnit.SECONDS));
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    java.util.logging.Logger.getLogger(SCMCache.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
    }

    @Override
    public File cloneRepository(SCMType scmType, String scmUrl, String revision)
            throws ScmException {
        try {
            SCMSpecifier spec = new SCMSpecifier(scmType, scmUrl, revision);

            if (cache.containsKey(spec)) {
                Optional<File> file = cache.get(spec).get();
                if (file.isPresent()) {
                    log.debug("Found cached repository for {} at {}", spec, file);
                    return file.get();
                }
            }

            File tempDir = Files.createTempDirectory("cloned_repo").toFile();
            log.debug("Cached repository for {} not found. Cloning to {}", spec, tempDir);
            try {
                scm.shallowCloneRepository(scmType, scmUrl, revision, tempDir);
                DirectoryReference ref = new DirectoryReference(tempDir);
                cache.put(spec, ref);
                return tempDir;
            } catch (ScmException ex) {
                try {
                    FileUtils.deleteDirectory(tempDir);
                } catch (IOException ioex) {
                    log.warn("Temporary directory could not be deleted", ex);
                }
                throw ex;
            }
        } catch (IOException ex) {
            throw new ScmException("Could not create temp directory for cloning the repository", ex);
        }
    }

    @Schedule(hour = "*", minute = "*/10")
    public void invalidateCache() {
        Iterator<Map.Entry<SCMSpecifier, DirectoryReference>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            DirectoryReference ref = it.next().getValue();
            if (!ref.check()) {
                it.remove();
            }
        }
    }

    @PreDestroy
    private void cleanup() {
        Iterator<Map.Entry<SCMSpecifier, DirectoryReference>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue().delete();
            it.remove();
        }
    }
}
