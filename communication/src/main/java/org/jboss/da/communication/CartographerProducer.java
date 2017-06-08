package org.jboss.da.communication;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.cartographer.CartographerCoreBuilder;
import org.commonjava.cartographer.INTERNAL.graph.discover.DiscovererImpl;
import org.commonjava.cartographer.graph.discover.meta.MetadataScannerSupport;
import org.commonjava.cartographer.graph.discover.patch.PatcherSupport;
import org.commonjava.cartographer.spi.graph.discover.DiscoverySourceManager;
import org.commonjava.cartographer.spi.graph.discover.ProjectRelationshipDiscoverer;
import org.commonjava.maven.atlas.graph.RelationshipGraphFactory;
import org.commonjava.maven.atlas.graph.spi.neo4j.FileNeo4jConnectionFactory;
import org.commonjava.maven.galley.auth.MemoryPasswordManager;
import org.commonjava.maven.galley.cache.FileCacheProviderConfig;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.config.TransportManagerConfig;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.filearc.FileTransportConfig;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMavenPluginImplications;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.rel.MavenModelProcessor;
import org.commonjava.maven.galley.maven.rel.ModelProcessorConfig;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.spi.transport.LocationResolver;
import org.commonjava.maven.galley.spi.transport.TransportManager;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.SimpleUrlLocationResolver;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.jboss.da.communication.pom.qualifier.DACartographerCore;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.io.File;
import java.io.IOException;

/**
 * Class used to inject null to '@Injects' in cartographer
 */
public class CartographerProducer {

    @Inject
    private MetadataScannerSupport metadataScannerSupport;

    @Inject
    private MavenPomReader mavenPomReader;

    @Inject
    private TransportManager transportManager;

    @Produces
    @DACartographerCore
    public CartographerCore getCartographerCore() throws IOException, CartoDataException {

        // tempFile is not really used, but just needed to be passed to the constructor
        File tempFile = new File("random");

        return new CartographerCoreBuilder(tempFile, new FileNeo4jConnectionFactory(null, true))
                .withDefaultTransports().withCache(getPartyLineCacheProvider()).build();
    }

    @Produces
    public NotFoundCache getNotFoundCache() {
        return new MemoryNotFoundCache();
    }

    @Produces
    public FileTransportConfig getFileTransportConfig() {
        return new FileTransportConfig();
    }

    @Produces
    public GlobalHttpConfiguration getGlobalHttpConfiguration() {
        return new GlobalHttpConfiguration();
    }

    @Produces
    public PasswordManager getPasswordManager() {
        return new MemoryPasswordManager();
    }

    @Produces
    public Http getHttp() {
        return new HttpImpl(getPasswordManager());
    }

    @Produces
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Produces
    public LocationExpander getLocationExpander() throws IOException, CartoDataException {
        return new NoOpLocationExpander();
    }

    @Produces
    public MavenPluginDefaults getMavenPluginDefaults() {
        return new StandardMaven304PluginDefaults();
    }

    @Produces
    public MavenPluginImplications getMavenPluginImplications() {
        return new StandardMavenPluginImplications(new XMLInfrastructure());
    }

    @Produces
    public ProjectRelationshipDiscoverer getProjectRelationshipDiscoverer() throws IOException,
            CartoDataException {
        return new DiscovererImpl(getMavenModelProcessor(), mavenPomReader, getGalleyMaven()
                .getArtifactManager(), getPatcherSupport(), metadataScannerSupport);
    }

    @Produces
    public GalleyMaven getGalleyMaven() throws IOException, CartoDataException {
        return getCartographerCore().getGalley();
    }

    @Produces
    public PatcherSupport getPatcherSupport() {
        return new PatcherSupport();
    }

    @Produces
    public FileEventManager getFileEventManager() throws IOException, CartoDataException {
        return new NoOpFileEventManager();
    }

    @Produces
    public TransportManagerConfig getTransportManagerConfig() {
        return new TransportManagerConfig();
    }

    @Produces
    public TransferDecorator getTransferDecorator() throws IOException, CartoDataException {
        return new NoOpTransferDecorator();
    }

    @Produces
    public ModelProcessorConfig getModelProcessorConfig() {
        ModelProcessorConfig disConf = new ModelProcessorConfig();
        disConf.setIncludeBuildSection(false);
        disConf.setIncludeManagedDependencies(false);
        disConf.setIncludeManagedPlugins(false);
        return disConf;
    }

    @Produces
    public MavenModelProcessor getMavenModelProcessor() {
        return new MavenModelProcessor();
    }

    @Produces
    public LocationResolver getLocationResolver() throws IOException, CartoDataException {
        return new SimpleUrlLocationResolver(getLocationExpander(), transportManager);
    }

    @Produces
    public PartyLineCacheProvider getPartyLineCacheProvider() throws IOException,
            CartoDataException {

        // tempFile is not really used, but just needed to be passed to the constructor
        File tempFile = new File("random");

        return new PartyLineCacheProvider(tempFile, new HashedLocationPathGenerator(),
                getFileEventManager(), getTransferDecorator());
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
