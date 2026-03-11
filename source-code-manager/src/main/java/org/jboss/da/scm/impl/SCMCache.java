package org.jboss.da.scm.impl;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class SCMCache {

    private static final Logger log = LoggerFactory.getLogger(SCMCache.class);

    @Inject
    ScmFacade scm;

    private final Map<SCMSpecifier, DirectoryReference> cache = new HashMap<>();

    private final Map<SCMSpecifier, FutureReference> futureCache = new HashMap<>();

    public FutureReference get(SCMSpecifier spec) {
        checkFutureCache();
        if (cache.containsKey(spec)) {
            DirectoryReference ref = cache.get(spec);
            Optional<File> file = ref.get();
            if (file.isPresent()) {
                return new FutureReference(ref);
            }
        }
        if (futureCache.containsKey(spec)) {
            return new FutureReference(futureCache.get(spec));
        }
        FutureReference future = new FutureReference();
        futureCache.put(spec, future);
        return future;
    }

    private void checkFutureCache() {
        Iterator<Map.Entry<SCMSpecifier, FutureReference>> it = futureCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<SCMSpecifier, FutureReference> e = it.next();
            FutureReference value = e.getValue();
            if (value.isDone()) {
                try {
                    cache.put(e.getKey(), value.get(1, TimeUnit.SECONDS));
                    it.remove();
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    log.error("Error while getting Future object.", ex);
                }
            }
        }
    }

    @Scheduled(every = "10m")
    public void invalidateCache() {
        checkFutureCache();
        Iterator<Map.Entry<SCMSpecifier, DirectoryReference>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            DirectoryReference ref = it.next().getValue();
            if (!ref.check()) {
                it.remove();
            }
        }
    }

    @PreDestroy
    void cleanup() {
        Iterator<Map.Entry<SCMSpecifier, DirectoryReference>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue().delete();
            it.remove();
        }
    }
}
