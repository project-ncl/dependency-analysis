package org.jboss.da.scm.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
class FutureReference {

    final CompletableFuture<DirectoryReference> futureReference;

    private final boolean shouldIComplete;

    public FutureReference() {
        futureReference = new CompletableFuture<>();
        shouldIComplete = true;
    }

    public FutureReference(DirectoryReference ref) {
        futureReference = CompletableFuture.completedFuture(ref);
        shouldIComplete = false;
    }

    public FutureReference(FutureReference fref) {
        this.futureReference = fref.futureReference;
        shouldIComplete = false;
    }

    public boolean shouldIComplete() {
        return shouldIComplete;
    }

    public boolean isDone() {
        return futureReference.isDone();
    }

    public void complete(DirectoryReference ref) {
        if (!shouldIComplete)
            throw new IllegalStateException(
                    "Completing when this instance shouldn't complete the future.");

        if (!futureReference.complete(ref))
            throw new IllegalStateException("Already completed.");
    }

    public void completeExceptionally(Exception ex) {
        if (!shouldIComplete)
            throw new IllegalStateException(
                    "Completing when this instance shouldn't complete the future.");
        futureReference.completeExceptionally(ex);
    }

    public DirectoryReference get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return futureReference.get(timeout, unit);
    }
}
