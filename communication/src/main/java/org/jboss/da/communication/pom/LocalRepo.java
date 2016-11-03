package org.jboss.da.communication.pom;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.jboss.da.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import static java.nio.file.Files.lines;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class holding local maven-like repository of pom files.
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class LocalRepo {

    private static final Logger log = LoggerFactory.getLogger(LocalRepo.class);

    private Path path;

    private static final String SUFFIX = "-20150205.044024-1.pom";

    public LocalRepo(GalleyMaven galley, File scmDir) throws IOException {
        path = Files.createTempDirectory("deps");

        initLocalRepo(galley, scmDir.toPath());
    }

    public synchronized Location getLocation() {
        return new SimpleLocation(getUri().toString());
    }

    public synchronized URI getUri() {
        return path.toUri();
    }

    private void initLocalRepo(GalleyMaven galley, Path scmDir) throws IOException {
        Set<Path> poms = getAllPoms(scmDir);

        for (Path pomFile : poms) {
            PomPeek peek = new PomPeek(pomFile.toFile());
            final ProjectVersionRef key = peek.getKey();
            if (key == null) {
                log.warn("Could not parse " + pomFile.toAbsolutePath());
                continue;
            }

            try {
                String artifactPath = ArtifactPathUtils.formatArtifactPath(key.asPomArtifact(),
                        galley.getTypeMapper());

                Path p = path.resolve(artifactPath);
                Files.createDirectories(p.getParent());
                if (key.getVersionSpec().isSnapshot()) {
                    initSnapshot(key, pomFile, p);
                } else {
                    Files.copy(pomFile, p);
                }
            } catch (TransferException | RuntimeException ex) {
                log.warn("Could not parse " + pomFile.toAbsolutePath(), ex);
            } catch (FileAlreadyExistsException ex) {
                log.error("File already exists. This is because there are multiple file with same "
                        + "GAV. This ususaly happens when there are pom files in tests and is "
                        + "harmless in this case.", ex);
            }
        }
    }

    public static Set<Path> getAllPoms(Path scmDir) throws IOException {
        return Files.walk(scmDir)
                .filter(p -> !Files.isDirectory(p)) // is file
                .filter(p -> p.endsWith("pom.xml")) // named pom.xml
                .collect(Collectors.toSet());
    }

    public synchronized Set<Path> getAllPoms() throws IOException {
        return Files.walk(path)
                .filter(p -> !Files.isDirectory(p)) // is file
                .filter(p -> p.toString().endsWith(".pom")) // name ends with .pom
                .collect(Collectors.toSet());
    }

    protected void delete() {
        File f;
        synchronized (this) {
            f = path.toFile();
            path = null;
        }
        FileUtils.deleteDirectory(f);
    }

    /**
     * This method will generate maven-metadata for 
     * @param key
     * @param pomFile
     * @param p
     * @throws IOException 
     */
    private void initSnapshot(ProjectVersionRef key, Path pomFile, Path p) throws IOException {
        Path dir = p.getParent();
        Path metadata = dir.resolve("maven-metadata.xml");
        if(Files.exists(metadata)){
            throw new UnsupportedOperationException("Merging of metadata is not supported yet. Conflicting metadata: " + metadata);
        }
        InputStream is = this.getClass().getResourceAsStream("/template/maven-metadata.xml");
        
        Stream<String> lines = new BufferedReader(new InputStreamReader(is)).lines()
                .map(l -> l.replace("${groupId}", key.getGroupId()))
                .map(l -> l.replace("${artifactId}", key.getGroupId()))
                .map(l -> l.replace("${version}", key.getVersionString().replace("-SNAPSHOT", "")));
                
        Files.write(metadata, (Iterable<String>) lines::iterator);
        Path newPomFile = dir.resolve(p.getFileName().toString().replace("-SNAPSHOT.pom", SUFFIX));
        Files.copy(pomFile, newPomFile);
    }
}
