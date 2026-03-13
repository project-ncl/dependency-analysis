package org.jboss.da.communication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Produces;

import org.commonjava.cdi.util.weft.config.DefaultWeftConfig;
import org.commonjava.cdi.util.weft.config.WeftConfig;
import org.commonjava.maven.galley.auth.MemoryPasswordManager;
import org.commonjava.maven.galley.cache.partyline.PartyLineCacheProvider;
import org.commonjava.maven.galley.config.TransportManagerConfig;
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
import org.commonjava.maven.galley.nfc.MemoryNotFoundCache;
import org.commonjava.maven.galley.proxy.NoOpProxySitesCache;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.PathGenerator;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;
import org.commonjava.maven.galley.spi.proxy.ProxySitesCache;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.commonjava.maven.galley.transport.NoOpLocationExpander;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalHttpConfiguration;
import org.commonjava.maven.galley.transport.htcli.conf.GlobalProxyConfig;
import org.commonjava.util.partyline.Partyline;

import com.fasterxml.jackson.databind.ObjectMapper;

// Inspiration taken from the previous CartographyProducer and org.commonjava.maven.galley.embed.TestCDIProvider.java
public class GalleyMavenProducer {

    private FileTransportConfig fileTransportConfig;

    private PartyLineCacheProvider cacheProvider;

    private GlobalHttpConfiguration globalHttpConfiguration;

    private PathGenerator pathGenerator;

    @PostConstruct
    void init() {
        try {
            File file = Files.createTempDirectory("galley").toFile();

            pathGenerator = new HashedLocationPathGenerator();
            cacheProvider = new PartyLineCacheProvider(
                    file,
                    pathGenerator,
                    new NoOpFileEventManager(),
                    new TransferDecoratorManager(),
                    Executors.newScheduledThreadPool(2),
                    new Partyline());
            fileTransportConfig = new FileTransportConfig(file, pathGenerator);

            globalHttpConfiguration = new GlobalHttpConfiguration();

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
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Produces
    public GlobalProxyConfig getGlobalProxyConfig() {
        return new GlobalProxyConfig() {
            @Override
            public String getHost() {
                return "proxy.com";
            }

            @Override
            public int getPort() {
                return 3128;
            }

            @Override
            public String getUser() {
                return null;
            }

            @Override
            public List<String> getAllowHttpJobTypes() {
                return new ArrayList<>();
            }

            @Override
            public List<String> getEgressSites() {
                return new ArrayList<>();
            }
        };
    }

    @Produces
    public ProxySitesCache getProxySitesCache() {
        return new NoOpProxySitesCache();
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
