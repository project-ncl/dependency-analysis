package org.jboss.da.communication;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.cdi.util.weft.config.DefaultWeftConfig;
import org.commonjava.cdi.util.weft.config.WeftConfig;
import org.commonjava.maven.galley.auth.MemoryPasswordManager;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.config.TransportManagerConfig;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.filearc.FileTransportConfig;
import org.commonjava.maven.galley.io.HashedLocationPathGenerator;
import org.commonjava.maven.galley.io.TransferDecoratorManager;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMaven304PluginDefaults;
import org.commonjava.maven.galley.maven.internal.defaults.StandardMavenPluginImplications;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.commonjava.maven.galley.maven.rel.MavenModelProcessor;
import org.commonjava.maven.galley.maven.rel.ModelProcessorConfig;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.util.partyline.JoinableFileManager;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;

// Inspiration taken from the previous CartographyProducer and org.commonjava.maven.galley.embed.TestCDIProvider.java
public class GalleyMavenProducer {

    @Inject
    private TransferDecoratorManager transferDecorator;

    private FileTransportConfig fileTransportConfig;

    private PartyLineCacheProvider cacheProvider;

    private MetricRegistry metricRegistry;

    private GlobalHttpConfiguration globalHttpConfiguration;

    private PathGenerator pathGenerator;

    private final TransportMetricConfig transportMetricConfig = new TransportMetricConfig() {
        public boolean isEnabled() {
            return false;
        }

        @Override
        public String getNodePrefix() {
            return null;
        }

        @Override
        public String getMetricUniqueName(Location location) {
            return null;
        }
    };

    @PostConstruct
    void init() {
        try {
            File file = Files.createTempDirectory("galley").toFile();

            pathGenerator = new HashedLocationPathGenerator();
            cacheProvider = new PartyLineCacheProvider(
                    file,
                    pathGenerator,
                    new NoOpFileEventManager(),
                    transferDecorator,
                    Executors.newScheduledThreadPool(2),
                    new JoinableFileManager());
            fileTransportConfig = new FileTransportConfig(file, pathGenerator);

            globalHttpConfiguration = new GlobalHttpConfiguration();

            metricRegistry = new MetricRegistry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Produces
    public FileTransportConfig getFileTransportConfig() {
        return fileTransportConfig;
    }

    @Produces
    public LocationExpander getLocationExpander() {
        return new NoOpLocationExpander();
    }

    @Produces
    public PartyLineCacheProvider getCacheProvider() {
        return cacheProvider;
    }

    @Produces
    public NotFoundCache getNotFoundCache() {
        return new MemoryNotFoundCache();
    }

    @Produces
    public MavenPluginImplications getMavenPluginImplications() {
        return new StandardMavenPluginImplications(new XMLInfrastructure());
    }

    @Produces
    public MavenPluginDefaults getMavenPluginDefaults() {
        return new StandardMaven304PluginDefaults();
    }

    @Produces
    public WeftConfig getWeftConfig() {
        return new DefaultWeftConfig();
    }

    @Produces
    public Http getHttp() {
        return new HttpImpl(getPasswordManager());
    }

    @Produces
    public PasswordManager getPasswordManager() {
        return new MemoryPasswordManager();
    }

    @Produces
    public PathGenerator getPathGenerator() {
        return pathGenerator;
    }

    @Produces
    public TransportManagerConfig getTransportManagerConfig() {
        return new TransportManagerConfig();
    }

    @Produces
    public FileEventManager getFileEventManager() {
        return new NoOpFileEventManager();
    }

    @Produces
    public GlobalHttpConfiguration getGlobalHttpConfiguration() {
        return globalHttpConfiguration;
    }

    @Produces
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    @Produces
    public TransportMetricConfig getTransportMetricConfig() {
        return transportMetricConfig;
    }

    @Produces
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    // Only appears to be used by GalleyWrapperTestIT
    @Produces
    public ModelProcessorConfig getModelProcessorConfig() {
        ModelProcessorConfig disConf = new ModelProcessorConfig();
        disConf.setIncludeBuildSection(false);
        disConf.setIncludeManagedDependencies(false);
        disConf.setIncludeManagedPlugins(false);
        return disConf;
    }

    // Only appears to be used by GalleyWrapperTestIT
    @Produces
    public MavenModelProcessor getMavenModelProcessor() {
        return new MavenModelProcessor();
    }
}
