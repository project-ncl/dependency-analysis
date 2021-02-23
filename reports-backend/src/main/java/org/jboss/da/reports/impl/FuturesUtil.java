package org.jboss.da.reports.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.jboss.da.common.CommunicationException;

public class FuturesUtil {
    static <T> List<T> joinFutures(List<CompletableFuture<T>> futures) throws CommunicationException {
        try {
            return futures.stream().map(r -> r.join()).collect(Collectors.toList());
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof CommunicationException) {
                throw (CommunicationException) ex.getCause();
            }
            throw ex;
        }
    }

    static <T> Set<T> joinFutures(Set<CompletableFuture<T>> futures) throws CommunicationException {
        try {
            return futures.stream().map(r -> r.join()).collect(Collectors.toSet());
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof CommunicationException) {
                throw (CommunicationException) ex.getCause();
            }
            throw ex;
        }
    }
}
