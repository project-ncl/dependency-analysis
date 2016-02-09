package org.jboss.da.communication.pom;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.jboss.da.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class holding local maven-like repository of pom files.
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class LocalRepo {

    private static Logger log = LoggerFactory.getLogger(LocalRepo.class);

    private Path path;

    public LocalRepo(GalleyMaven galley, File scmDir) throws IOException {
        path = Files.createTempDirectory("deps");

        initLocalRepo(galley, scmDir.toPath());
    }

    public synchronized Location getLocation() {
        return new SimpleLocation(path.toUri().toString());
    }

    private void initLocalRepo(GalleyMaven galley, Path scmDir) throws IOException {
        Set<Path> poms = getAllPoms(scmDir);

        for (Path pomFile : poms) {
            PomPeek peek = new PomPeek(pomFile.toFile());
            if (peek.getKey() == null) {
                log.warn("Could not parse " + pomFile.toAbsolutePath());
                continue;
            }

            try {
                String artifactPath = ArtifactPathUtils.formatArtifactPath(peek.getKey()
                        .asPomArtifact(), galley.getTypeMapper());

                Path p = path.resolve(artifactPath);
                Files.createDirectories(p.getParent());
                Files.copy(pomFile, p);
            } catch (TransferException | RuntimeException ex) {
                log.warn("Could not parse " + pomFile.toAbsolutePath(), ex);
            }
        }
    }

    private Set<Path> getAllPoms(Path scmDir) throws IOException {
        return Files.walk(scmDir)
                .filter(p -> !Files.isDirectory(p)) // is file
                .filter(p -> p.endsWith("pom.xml")) // named pom.xml
                .collect(Collectors.toSet());
    }

    public Set<Path> getAllPoms() throws IOException {
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

}
