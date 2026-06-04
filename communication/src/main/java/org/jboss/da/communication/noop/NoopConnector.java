package org.jboss.da.communication.noop;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.repository.api.RepositoryConnector;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

import io.quarkus.arc.lookup.LookupIfProperty;

@Noop
@ApplicationScoped
@LookupIfProperty(name = "da.repository-manager-support", stringValue = "DISABLED")
public class NoopConnector implements RepositoryConnector {
    @Override
    public List<String> getVersionsOfGA(GA ga) throws RepositoryException {
        return List.of();
    }

    @Override
    public List<String> getVersionsOfNpm(String packageName) throws RepositoryException {
        return List.of();
    }

    @Override
    public Optional<MavenProject> getPom(GAV gav) throws RepositoryException {
        return Optional.empty();
    }

    @Override
    public Optional<InputStream> getPomStream(GAV gav) throws RepositoryException {
        return Optional.empty();
    }

    @Override
    public boolean doesGAVExistInPublicRepo(GAV gav) throws RepositoryException {
        return false;
    }
}
