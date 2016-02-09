package org.jboss.da.communication.pom;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.jboss.da.communication.model.GAV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

    public Artifact getPom(String pomPath) throws PomAnalysisException {
        Artifact a = new Artifact(scm, pomPath);
        if (a.ref == null) {
            throw new PomAnalysisException("Could not analyse pom " + scm.resolve(pomPath));
        }
        return a;
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
     * Returns first level dependencies of an artifact.
     * You need to set locations so the galley know where to look for dependencies.
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

    private static GAV generateGAV(DependencyView dep) {
        try {
            return new GAV(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
        } catch (GalleyMavenException ex) {
            log.warn("Failed to get gav of dependency " + dep, ex);
            return null;
        }
    }

    // private void addLocations(PomPeek pp, MavenPomView view){
    // view.getAllRepositories();
    // }

    public void addLocations(List<String> repositories) {
        repositories.stream()
                .map(repository -> new SimpleLocation(repository, repository))
                .forEachOrdered(locations::add);
    }

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
     * Add maven central repo to locations.
     */
    public void addCentralLocation() {
        locations.add(new SimpleLocation("central", "http://repo.maven.apache.org/maven2/"));
    }

    @Override
    public void close() {
        localRepo.delete();
    }

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
            this(basePath.resolve(pomPath));
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
    }
}
