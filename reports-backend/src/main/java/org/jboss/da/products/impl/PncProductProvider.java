package org.jboss.da.products.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.PncConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.products.impl.PncProductProvider.Pnc;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.jboss.pnc.dto.requests.QValue;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:pkocandr@redhat.com">Petr Kocandrle</a>
 */
@Pnc
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@RequestScoped
public class PncProductProvider extends AbstractProductProvider {

    @Inject
    private PncConnector pncConnector;

    private Set<QValue> qualifiers = Set.of();

    /**
     * Sets what qualifiers should be retrieved from PNC on a request.
     *
     * @param qualifiers to fetch
     */
    public void setQualifiers(Set<QValue> qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    Stream<QualifiedVersion> getVersionsStreamMaven(GA ga) {
        if (!ga.isValid()) {
            userLog.warn("Received nonvalid GA " + ga + ", using empty list of versions.");
            log.warn("Received nonvalid GA: " + ga);
            return Stream.empty();
        }
        try {
            List<QualifiedVersion> versionsOfGA;
            versionsOfGA = pncConnector.getMavenVersions(ga, mode, qualifiers);
            log.debug("Got versions of " + ga + " from PNC: " + versionsOfGA);
            return versionsOfGA.stream();
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    @Override
    Stream<QualifiedVersion> getVersionsStreamNPM(String name) {
        try {
            List<QualifiedVersion> versionsOfGA;
            versionsOfGA = pncConnector.getNpmVersions(name, mode, qualifiers);
            log.debug("Got versions of " + name + " from PNC: " + versionsOfGA);
            return versionsOfGA.stream();
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    public static @interface Pnc {
    }

}
