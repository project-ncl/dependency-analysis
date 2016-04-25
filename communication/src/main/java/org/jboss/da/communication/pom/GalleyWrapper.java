package org.jboss.da.communication.pom;

import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.graph.MavenModelProcessor;
import org.commonjava.cartographer.graph.discover.DiscoveryConfig;
import org.commonjava.cartographer.graph.discover.DiscoveryResult;
import org.commonjava.maven.atlas.graph.rel.DependencyRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.MavenGAVView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.jboss.da.model.rest.GAV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class GalleyWrapper implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(GalleyWrapper.class);

    private final MavenPomReader mvnPomReader;

    private final List<Location> locations = new ArrayList<>();

    private final Path scm;

    private final LocalRepo localRepo;

    public GalleyWrapper(GalleyMaven galley, File scmDir) throws IOException {
        this.mvnPomReader = galley.getPomReader();
        this.scm = scmDir.toPath();
        this.localRepo = new LocalRepo(galley, scmDir);
        locations.add(localRepo.getLocation());
    }

    /**
     * Return GalleyWrapper pointer to artifact specified in given pom.
     * @param pomPath Path to the pom file relative to the scm repostiory root.
     * @return GalleyWrapper "pointer" to an artifact.
     * @throws PomAnalysisException 
     */
    public Artifact getPom(String pomPath) throws PomAnalysisException {
        Artifact a = new Artifact(scm, pomPath);
        if (a.ref == null) {
            throw new PomAnalysisException("Could not analyse pom " + scm.resolve(pomPath));
        }
        return a;
    }

    /**
     * Returns GalleyWrapper pointer to artifact representing given GAV.
     * @param gav GAV of the artifact.
     * @return GalleyWrapper "pointer" to an artifact.
     * @throws org.jboss.da.communication.pom.PomAnalysisException
     */
    public Artifact getGAV(GAV gav) throws PomAnalysisException {
        try {
            for (Path p : LocalRepo.getAllPoms(scm)) {
                PomPeek pp = new PomPeek(p.toFile());
                if (pp.getKey() == null)
                    continue;
                if (gav.equals(generateGAV(pp.getKey()))) {
                    return new Artifact(p);
                }
            }
        } catch (IOException ex) {
            throw new PomAnalysisException(ex);
        }
        throw new PomAnalysisException("Artifact " + gav + " was not found in repository");
    }

    /**
     * Return modules of artifact, including the artifact itselve.
     * Module poms that couldn't be parsed are ommited.
     * @param artifact root artifact.
     * @return Set of all modules.
     */
    public Set<Artifact> getModules(Artifact artifact){
        return artifact.pp.getModules().stream()
                .map(m -> artifact.path.resolve(m))
                .map(Artifact::new)
                .filter(a -> a.ref != null)
                .collect(Collectors.toSet());
    }

    /**
     * Return all (transitive) modules of artifact, including the artifact itselve.
     * Module poms that couldn't be parsed are ommited.
     * @param artifact root artifact.
     * @return Set of all modules.
     */
    public Set<Artifact> getAllModules(Artifact artifact) {
        Set<Artifact> ret = new HashSet<>();
        fillModules(ret, artifact);
        return ret;
    }

    private void fillModules(Set<Artifact> modules, Artifact artifact) {
        modules.add(artifact);
        for (Artifact a : getModules(artifact)) {
            fillModules(modules, a);
        }
    }

    /**
     * Returns {@link MavenPomView} for given artifact.
     * You need to set locations so the galley know where to look for dependencies.
     * @param artifact
     * @return MavenPomView of the artifact
     * @throws org.jboss.da.communication.pom.PomAnalysisException
     * @see #addCentralLocation()
     * @see #addLocations(java.util.List)
     * @see #addLocationsFromPoms(org.jboss.da.communication.pom.PomReader)
     */
    public MavenPomView getPomView(Artifact artifact) throws PomAnalysisException {
        try {
            return mvnPomReader.read(artifact.ref, locations);
        } catch (GalleyMavenException ex) {
            throw new PomAnalysisException(ex);
        }
    }

    /**
     * Returns first level dependencies of an artifact.
     * You need to set locations so the galley know where to look for dependencies.
     * @param artifact Dependencies of this artifact will be returned
     * @return First level dependencies.
     * @throws org.jboss.da.communication.pom.PomAnalysisException
     * @see #addCentralLocation()
     * @see #addLocations(java.util.List)
     * @see #addLocationsFromPoms(org.jboss.da.communication.pom.PomReader)
     */
    public Set<GAV> getDependencies(Artifact artifact) throws PomAnalysisException{
        ProjectVersionRef ref = artifact.ref;
        List<DependencyView> allDirectDependencies;
        try{
            MavenPomView view = mvnPomReader.read(ref, locations);
            allDirectDependencies = view.getAllDirectDependencies();
        }catch(GalleyMavenException ex){
            throw new PomAnalysisException(ex);
        }

        return allDirectDependencies.stream()
                .map(GalleyWrapper::generateGAV)
                .filter(x -> x != null)
                .collect(Collectors.toSet());
    }

    /**
     * Add repositories Galley should use when resolving dependencies.
     * @param repositories List of repository URLs.
     */
    public void addLocations(List<String> repositories) {
        repositories.stream()
                .map(repository -> new SimpleLocation(repository, repository))
                .forEachOrdered(locations::add);
    }

    /**
     * Add repositories Gally should use when resolving dependencies by
     * analysing pom files in SCM repository.
     * @param pomReader instance of {@link PomReader} used for parsing pom files
     * @throws IOException 
     */
    public void addLocationsFromPoms(PomReader pomReader) throws IOException{
        Set<Path> allPoms = localRepo.getAllPoms();
        allPoms.stream()
                .map(Path::toFile)
                .map(pomReader::analyze) // parse pom file
                .filter(Optional::isPresent).map(Optional::get) // filter sucessfuly parsed
                .map(p -> p.getMavenRepositories()).filter(r -> r != null) // get <repositories>
                .flatMap(r -> r.stream()) // stream of <repository>
                .map(r -> new SimpleLocation(r.getId(), r.getUrl()))
                .forEach(l -> locations.add(l));
    }

    /**
     * Add maven central repository among repositories Gally should use when
     * resolving dependencies.
     */
    public void addCentralLocation() {
        locations.add(new SimpleLocation("central", "http://repo.maven.apache.org/maven2/"));
    }

    @Override
    public void close() {
        localRepo.delete();
    }

    /**
     * Return all transitive dependencies of given artifact.
     * Dependencies of test-scope and provided-scope dependencies are ommited.
     * You need to set locations so the galley know where to look for dependencies.
     * @param artifact Dependencies of this artifact will be returned
     * @return Set of dependency relationships describing the dependency graph.
     * @throws GalleyMavenException
     * @throws CartoDataException 
     * @see #addCentralLocation()
     * @see #addLocations(java.util.List)
     * @see #addLocationsFromPoms(org.jboss.da.communication.pom.PomReader)
     */
    public Set<DependencyRelationship> getAllDependencies(Artifact artifact)
            throws GalleyMavenException, CartoDataException {
        return getAllDependencies(artifact, false, false);
    }

    /**
     * Return all transitive dependencies of given artifact.
     * You need to set locations so the galley know where to look for dependencies.
     * @param artifact Dependencies of this artifact will be returned
     * @param testDeps true if should dependencies of test-scope dependency be
     * resolved.
     * @param providedDeps true if should dependencies of provided-scope
     * dependency be resolved.
     * @return Set of dependency relationships describing the dependency graph. 
     * @throws GalleyMavenException
     * @throws CartoDataException 
     * @see #addCentralLocation()
     * @see #addLocations(java.util.List)
     * @see #addLocationsFromPoms(org.jboss.da.communication.pom.PomReader)
     */
    public Set<DependencyRelationship> getAllDependencies(Artifact artifact, boolean testDeps,
            boolean providedDeps) throws GalleyMavenException, CartoDataException {
        Set<DependencyRelationship> deps = new HashSet<>();

        URI src = localRepo.getUri();
        DiscoveryConfig disConf = new DiscoveryConfig(src);
        disConf.setLocations(locations);
        disConf.setIncludeBuildSection(false);
        disConf.setIncludeManagedDependencies(false);
        disConf.setIncludeManagedPlugins(false);
        MavenModelProcessor processor = new MavenModelProcessor();

        Queue<DependencyRelationship> work = new LinkedList<>();
        work.addAll(getDeps(artifact.ref, processor, src, disConf));

        while (!work.isEmpty()) {
            DependencyRelationship dr = work.remove();
            if (deps.contains(dr)) {
                continue;
            }
            deps.add(dr);

            if (!shouldAnalyzeDependencies(dr, testDeps, providedDeps))
                continue;

            try {
                work.addAll(getDeps(dr.getTarget(), processor, src, disConf));
            } catch (CartoDataException | GalleyMavenException ex) {
                log.warn("Failed to get dependencies for " + dr.getTarget(), ex);
            }
        }
        return deps;
    }

    private Set<DependencyRelationship> getDeps(ProjectVersionRef ref, MavenModelProcessor processor, URI src, DiscoveryConfig disConf) throws GalleyMavenException, CartoDataException{
        MavenPomView pomView = mvnPomReader.read(ref, locations);
        DiscoveryResult relationships = processor.readRelationships(pomView, src, disConf);
        return relationships.getAcceptedRelationships().stream()
                .filter(r -> r.getType() == RelationshipType.DEPENDENCY)
                .map(r -> (DependencyRelationship) r)
                .collect(Collectors.toSet());
    }

    /**
     * Return true if scope is compile, runtime, test (when {@code testDeps} is
     * true) or provided (when {@code providedDeps} is true).
     * @param dr Dependency relationship which scope we compare.
     * @param testDeps
     * @param providedDeps
     * @return 
     */
    public static boolean shouldAnalyzeDependencies(DependencyRelationship dr, boolean testDeps,
            boolean providedDeps) {
        switch (dr.getScope()) {
            case _import:
            case embedded:
            case system:
            case toolchain:
                return false;
            case test:
                return testDeps;
            case provided:
                return providedDeps;
            case compile:
            case runtime:
            default:
                return true;
        }
    }

    private static GAV generateGAV(MavenGAVView dep) {
        try {
            return new GAV(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
        } catch (GalleyMavenException ex) {
            log.warn("Failed to get gav of dependency " + dep, ex);
            return null;
        }
    }

    public static GAV generateGAV(ProjectVersionRef dep) {
        return new GAV(dep.getGroupId(), dep.getArtifactId(), dep.getVersionString());
    }

    /**
     * GalleyWrapper "pointer" to an arifact.
     */
    public static class Artifact {

        private Path path;

        private PomPeek pp;

        private ProjectVersionRef ref;

        private Artifact(final Path p) {
            path = p;
            if (path.endsWith("pom.xml"))
                path = path.getParent();
            pp = new PomPeek(path.resolve("pom.xml").toFile());
            ref = pp.getKey();
        }

        private Artifact(final Path basePath, final String pomPath) {
            this(basePath.resolve(toRelative(pomPath)));
        }

        @Override
        public String toString() {
            return "GalleyWrapper.Artifact{" + "path=" + path + '}';
        }

        public GAV getGAV() throws PomAnalysisException {
            if (ref == null)
                throw new PomAnalysisException("Failed to get gav from " + path);

            return new GAV(ref.getGroupId(), ref.getArtifactId(), ref.getVersionString());
        }

        private static String toRelative(String path) {
            if (path.startsWith("/")) {
                return path.replaceFirst("/", "");
            } else {
                return path;
            }
        }
    }
}
