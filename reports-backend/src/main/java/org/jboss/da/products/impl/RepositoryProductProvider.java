package org.jboss.da.products.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.indy.api.IndyConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.products.impl.RepositoryProductProvider.Repository;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Repository
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@RequestScoped
public class RepositoryProductProvider extends AbstractProductProvider {

    @Inject
    private IndyConnector indyConnector;

    @Override
    Stream<QualifiedVersion> getVersionsStreamMaven(GA ga) {
        if (!ga.isValid()) {
            userLog.warn("Received nonvalid GA " + ga + ", using empty list of versions.");
            log.warn("Received nonvalid GA: " + ga);
            return Stream.empty();
        }
        try {
            List<String> versionsOfGA;
            versionsOfGA = indyConnector.getVersionsOfGA(ga);
            log.debug("Got versions of " + ga + " from repository: " + versionsOfGA);
            return versionsOfGA.stream().map(QualifiedVersion::new);
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    @Override
    Stream<QualifiedVersion> getVersionsStreamNPM(String name) {
        try {
            List<String> versionsOfGA;
            versionsOfGA = indyConnector.getVersionsOfNpm(name);
            log.debug("Got versions of " + name + " from repository: " + versionsOfGA);
            return versionsOfGA.stream().map(QualifiedVersion::new);
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
