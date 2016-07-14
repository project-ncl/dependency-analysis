package org.jboss.da.communication;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.cartographer.CartographerCoreBuilder;
import org.commonjava.cartographer.spi.graph.discover.DiscoverySourceManager;
import org.commonjava.maven.atlas.graph.RelationshipGraphFactory;
import org.commonjava.maven.atlas.graph.spi.neo4j.FileNeo4jConnectionFactory;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.jboss.da.communication.pom.qualifier.DACartographerCore;

import javax.enterprise.inject.Produces;

import java.io.File;
import java.io.IOException;

/**
 * Class used to inject null to '@Injects' in cartographer
 */
public class CartographerProducer {

    @Produces
    @DACartographerCore
    public CartographerCore getCartographerCore() throws IOException, CartoDataException {

        // tempFile is not really used, but just needed to be passed to the constructor
        File tempFile = new File("random");

        PartyLineCacheProvider cache = new PartyLineCacheProvider(tempFile,
                new HashedLocationPathGenerator(), new NoOpFileEventManager(),
                new NoOpTransferDecorator());

        return new CartographerCoreBuilder(tempFile, new FileNeo4jConnectionFactory(null, true))
                .withDefaultTransports().withCache(cache).build();
    }

    @Produces
    public DiscoverySourceManager getDiscoverySourceManager() {
        return null;
    }

    @Produces
    public RelationshipGraphFactory getRelationshipGraphFactory() {
        return null;
    }

    @Produces
    public FileCacheProviderConfig getFileCacheProviderConfig() {
        FileCacheProviderConfig fcpc = new FileCacheProviderConfig(new File("random"));
        return fcpc;
    }
}
