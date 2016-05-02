package org.jboss.da.communication.pom;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.maven.atlas.graph.rel.DependencyRelationship;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.pom.qualifier.DACartographerCore;
import org.jboss.da.communication.pom.impl.DependencyTreeBuilder;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PomAnalyzerImpl implements PomAnalyzer {

    @Inject
    private Logger log;

    @Inject
    private PomReader pomReader;

    @Inject
    @DACartographerCore
    private CartographerCore carto;

    @Inject
    private Configuration config;

    @Inject
    private DependencyTreeBuilder dtb;

    @Override
    public GAVDependencyTree readRelationships(File pomRepoDir, String pomPath,
            List<String> repositories) throws PomAnalysisException {

        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), pomRepoDir)) {
            GalleyWrapper.Artifact pom = gw.getPom(pomPath);
            gw.addLocations(repositories);

            return readRelationships(gw, pom);
        } catch (IOException ex) {
            throw new PomAnalysisException(ex);
        }
    }

    @Override
    public GAVDependencyTree readRelationships(File pomRepoDir, GAV gav)
            throws PomAnalysisException {
        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), pomRepoDir)) {
            GalleyWrapper.Artifact artifact = gw.getGAV(gav);

            return readRelationships(gw, artifact);
        } catch (IOException ex) {
            throw new PomAnalysisException(ex);
        }
    }

    private GAVDependencyTree readRelationships(GalleyWrapper gw, GalleyWrapper.Artifact a)
            throws PomAnalysisException {
        try {
            gw.addDefaultLocations(config);
            gw.addLocationsFromPoms(pomReader);

            Set<DependencyRelationship> relationships = gw.getAllDependencies(a);

            GAV originGAV = a.getGAV();

            return dtb.getDependencyTree(relationships, originGAV, false, false);
        } catch (CartoDataException | GalleyMavenException | IOException ex) {
            throw new PomAnalysisException(ex);
        }
    }

    @Override
    public Set<GAV> getToplevelDepency(File pomRepoDir, GAV gav) throws PomAnalysisException {
        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), pomRepoDir)) {
            GalleyWrapper.Artifact artifact = gw.getGAV(gav);

            gw.addDefaultLocations(config);
            gw.addLocationsFromPoms(pomReader);

            return gw.getDependencies(artifact);
        } catch (IOException ex) {
            throw new PomAnalysisException(ex);
        }
    }

    @Override
    public Optional<File> getPOMFileForGAV(File pomRepoDir, GAV gav) {
        try{
            return LocalRepo.getAllPoms(pomRepoDir.toPath()).stream()
                    .filter(p -> isProjectVersionRefSameAsGAV(p, gav))
                    .map(Path::toFile)
                    .findAny();
        }catch(IOException ex){
            log.warn("Failed to find pom for GAV", ex);
            return Optional.empty();
        }
    }

    @Override
    public MavenPomView getGitPomView(File repoDir, String pomPath, List<String> repositories)
            throws PomAnalysisException {
        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), repoDir)) {
            GalleyWrapper.Artifact pom = gw.getPom(pomPath);
            gw.addDefaultLocations(config);
            gw.addLocationsFromPoms(pomReader);
            gw.addLocations(repositories);

            return gw.getPomView(pom);
        } catch (IOException ex) {
            throw new PomAnalysisException(ex);
        }
    }

    @Override
    public MavenPomView getMavenPomView(InputStream is) throws ConfigurationParseException,
            GalleyMavenException {
        PomPeek pom = new PomPeek(is);

        if (pom.getKey() == null) {
            log.warn("Could not parse pom for GAV");
        }
        StringBuilder query = new StringBuilder();
        DAConfig cfg = this.config.getConfig();
        query.append(cfg.getAproxServer());
        query.append("/api/group/public/");
        Location repoLocation = new SimpleLocation(query.toString());

        List<Location> repos = new ArrayList<>();
        repos.add(repoLocation);

        MavenPomReader mavenPomReader = carto.getGalley().getPomReader();
        MavenPomView pomView = mavenPomReader.read(pom.getKey(), repos);

        return pomView;
    }

    @Override
    public Map<GA, Set<GAV>> getDependenciesOfModules(File scmDir, String pomPath,
            List<String> repositories) throws PomAnalysisException {
        try (GalleyWrapper wrapper = new GalleyWrapper(carto.getGalley(), scmDir)) {
            wrapper.addDefaultLocations(config);
            wrapper.addLocations(repositories);
            wrapper.addLocationsFromPoms(pomReader);

            GalleyWrapper.Artifact rootPom = wrapper.getPom(pomPath);

            Set<GalleyWrapper.Artifact> allModules = wrapper.getAllModules(rootPom);

            Map<GA, Set<GAV>> ret = new HashMap<>();
            for (GalleyWrapper.Artifact a : allModules) {
                try {
                    Set<GAV> dependencies = wrapper.getDependencies(a);
                    ret.put(a.getGAV().getGA(), dependencies);
                } catch (PomAnalysisException ex) {
                    log.warn("Failed to get dependencies for module " + a, ex);
                }
            }
            return ret;
        } catch (IOException | PomAnalysisException ex) {
            throw new PomAnalysisException("Failted to get dependencies of modules for "
                    + new File(scmDir, pomPath), ex);
        }
    }

    @Override
    public Optional<MavenProject> readPom(File pomPath) {
        return pomReader.analyze(pomPath);
    }

    @Override
    public Optional<MavenProject> readPom(InputStream is) throws CommunicationException {
        return pomReader.analyze(is);
    }

    private boolean isProjectVersionRefSameAsGAV(Path file, GAV gav) {
        PomPeek pk = new PomPeek(file.toFile());

        if (pk.getKey() == null) {
            return false;
        } else {
            return gav.equals(GalleyWrapper.generateGAV(pk.getKey()));
        }
    }

}
