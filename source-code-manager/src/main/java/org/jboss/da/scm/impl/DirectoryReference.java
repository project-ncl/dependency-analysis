package org.jboss.da.scm.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.jboss.da.scm.api.SCM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class DirectoryReference {

    Logger log = LoggerFactory.getLogger(DirectoryReference.class);

    private WeakReference<File> reference;

    private final String directoryPath;

    private Instant removeAt;

    private boolean deleted = false;

    public DirectoryReference(File directory) {
        this.directoryPath = directory.getAbsolutePath();
        setReference(directory);
    }

    private void setReference(File directory) {
        this.reference = new WeakReference<>(directory);
        this.removeAt = Instant.MAX;
    }

    /**
     * Returns File object referencing directory. When the directory doesn't exists, returns empty optional.
     * 
     * @return Optional of referenced directory.
     */
    public synchronized Optional<File> get() {
        if (deleted) {
            return Optional.empty();
        }
        File directory = reference.get();
        if (directory == null) {
            directory = new File(directoryPath);
            if (directory.canRead() && directory.isDirectory()) {
                setReference(directory);
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(directory);
    }

    /**
     * Check the status of the reference. If the reference is still active, return true. If the reference is not usable
     * anymore, return false.
     */
    synchronized boolean check() {
        if (deleted) {
            return false;
        }
        if (reference.get() != null) {
            return true;
        }
        if (removeAt.equals(Instant.MAX)) {
            removeAt = Instant.now().plus(SCM.TIME_TO_KEEP);
            return true;
        }
        if (Instant.now().isAfter(removeAt)) {
            delete();
            return false;
        }
        return true;
    }

    synchronized public void delete() {
        deleted = true;
        File directory = new File(directoryPath);
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException ex) {
            log.warn("Temporary directory could not be deleted", ex);
        }
    }

}
