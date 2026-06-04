package org.jboss.da.products.impl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.transaction.Transactional;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.repository.api.RepositoryConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.products.impl.RepositoryProductProvider.Repository;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Repository
@Transactional(Transactional.TxType.NOT_SUPPORTED)
@RequestScoped
public class RepositoryProductProvider extends AbstractProductProvider {

    @Inject
    IndyConnector indyConnector;
    private final RepositoryConnector repositoryConnector;

    @Override
    Stream<String> getVersionsStreamMaven(GA ga) {
        if (!ga.isValid()) {
            userLog.warn("Received nonvalid GA {}, using empty list of versions.", ga);
            log.warn("Received nonvalid GA: {}", ga);
            return Stream.empty();
        }

        try {
            List<String> versionsOfGA = repositoryConnector.getVersionsOfGA(ga);
            log.debug("Got versions of {} from repository: {}", ga, versionsOfGA);
            return versionsOfGA.stream();
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    @Override
    Stream<String> getVersionsStreamNPM(String name) {
        try {
            List<String> versionsOfNpm = repositoryConnector.getVersionsOfNpm(name);
            log.debug("Got versions of {} from repository: {}", name, versionsOfNpm);
            return versionsOfNpm.stream();
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    public static @interface Repository {
    }

}
