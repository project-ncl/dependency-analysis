package org.jboss.da.communication;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.cartographer.CartographerCoreBuilder;
import org.commonjava.cartographer.spi.graph.discover.DiscoverySourceManager;
import org.commonjava.maven.atlas.graph.RelationshipGraphFactory;
import org.commonjava.maven.atlas.graph.spi.neo4j.FileNeo4jConnectionFactory;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.cache.CacheProvider;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.jboss.da.communication.pom.qualifier.DACartographerCore;
import javax.enterprise.inject.Produces;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class used to inject null to '@Injects' in cartographer
 */
public class CartographerProducer {

    @Produces
    @DACartographerCore
    public CartographerCore getCartographerCore() throws IOException, CartoDataException {

        // tempFile is not really used, but just needed to be passed to the constructor
        File tempFile = new File("random");

        return new CartographerCoreBuilder(tempFile, new FileNeo4jConnectionFactory(null, true))
                .withDefaultTransports().build();
    }

    @Produces
    public Http getHttp() {
        return null;
    }

    @Produces
    public LocationExpander getLocationExpander() {
        return null;
    }

    @Produces
    public ArtifactManager getArtifactManager() {
        return null;
    }

    @Produces
    public DiscoverySourceManager getDiscoverySourceManager() {
        return null;
    }

    @Produces
    public LocationResolver getLocationResolver() {
        return null;
    }

    @Produces
    public RelationshipGraphFactory getRelationshipGraphFactory() {
        return null;
    }

    @Produces
    public MavenPomReader getMavenPomReader() {
        return null;
    }

    @Produces
    public FileCacheProviderConfig getFileCacheProviderConfig() {
        return null;
    }

    @Produces
    public NotFoundCache getNotFoundCache() {
        return null;
    }

    @Produces
    public CacheProvider getCacheProvider() {
        return null;
    }

    @Produces
    public TransferDecorator getTransferDecorator() {
        return null;
    }

    @Produces
    public ObjectMapper getObjectMapper() {
        return null;
    }

    @Produces
    public PathGenerator getPathGenerator() {
        return null;
    }

    @Produces
    public FileEventManager getFileEventManager() {
        return null;
    }

    @Produces
    public PasswordManager getPasswordManager() {
        return null;
    }
}
