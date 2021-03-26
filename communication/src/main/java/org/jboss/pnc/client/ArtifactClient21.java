package org.jboss.pnc.client;

import java.lang.Deprecated;
import java.lang.String;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.rest.api.endpoints.ArtifactEndpoint21;

public class ArtifactClient21 extends ClientBase21<ArtifactEndpoint21> {
  public ArtifactClient21(Configuration configuration) {
    super(configuration, ArtifactEndpoint21.class);
  }

  @Deprecated
  public RemoteCollection<ArtifactInfo> getAllFiltered(String identifier,
      Set<ArtifactQuality> qualities, RepositoryType repoType) throws RemoteResourceException {
    return getAllFiltered(identifier, qualities, repoType, java.util.Collections.emptySet());
  }

  public RemoteCollection<Artifact> getAll(String sha256, String md5, String sha1,
      Optional<String> sort, Optional<String> query) throws RemoteResourceException {
    try {
      PageReader pageLoader = new PageReader<>((pageParameters) -> { setSortAndQuery(pageParameters, sort, query); return getEndpoint().getAll(pageParameters, sha256, md5, sha1);}, getRemoteCollectionConfig());
      return pageLoader.getCollection();
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          PageReader pageLoader = new PageReader<>((pageParameters) -> { setSortAndQuery(pageParameters, sort, query); return getEndpoint().getAll(pageParameters, sha256, md5, sha1);}, getRemoteCollectionConfig());
          return pageLoader.getCollection();
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<Artifact> getAll(String sha256, String md5, String sha1) throws
      RemoteResourceException {
    try {
      return getAll(sha256, md5, sha1, Optional.empty(), Optional.empty());
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getAll(sha256, md5, sha1, Optional.empty(), Optional.empty());
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<ArtifactInfo> getAllFiltered(String identifier,
      Set<ArtifactQuality> qualities, RepositoryType repoType, Set<BuildCategory> buildCategories)
      throws RemoteResourceException {
    try {
      PageReader pageLoader = new PageReader<>((pageParameters) -> {  return getEndpoint().getAllFiltered(pageParameters, identifier, qualities, repoType, buildCategories);}, getRemoteCollectionConfig());
      return pageLoader.getCollection();
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          PageReader pageLoader = new PageReader<>((pageParameters) -> {  return getEndpoint().getAllFiltered(pageParameters, identifier, qualities, repoType, buildCategories);}, getRemoteCollectionConfig());
          return pageLoader.getCollection();
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public Artifact getSpecific(String id) throws RemoteResourceException {
    try {
      return getEndpoint().getSpecific(id);
    } catch (NotFoundException e) {
      throw new RemoteResourceNotFoundException(e);
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getEndpoint().getSpecific(id);
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public Artifact create(Artifact artifact) throws RemoteResourceException {
    try {
      return getEndpoint().create(artifact);
    } catch (NotFoundException e) {
      throw new RemoteResourceNotFoundException(e);
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getEndpoint().create(artifact);
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public void update(String id, Artifact artifact) throws RemoteResourceException,
      RemoteResourceNotFoundException {
    try {
      getEndpoint().update(id, artifact);
    } catch (NotFoundException e) {
      throw new RemoteResourceNotFoundException(e);
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          getEndpoint().update(id, artifact);
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public ArtifactRevision createQualityLevelRevision(String id, String quality, String reason)
      throws RemoteResourceException {
    try {
      return getEndpoint().createQualityLevelRevision(id, quality, reason);
    } catch (NotFoundException e) {
      throw new RemoteResourceNotFoundException(e);
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getEndpoint().createQualityLevelRevision(id, quality, reason);
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<Build> getDependantBuilds(String id, Optional<String> sort,
      Optional<String> query) throws RemoteResourceException {
    try {
      PageReader pageLoader = new PageReader<>((pageParameters) -> { setSortAndQuery(pageParameters, sort, query); return getEndpoint().getDependantBuilds(id, pageParameters);}, getRemoteCollectionConfig());
      return pageLoader.getCollection();
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          PageReader pageLoader = new PageReader<>((pageParameters) -> { setSortAndQuery(pageParameters, sort, query); return getEndpoint().getDependantBuilds(id, pageParameters);}, getRemoteCollectionConfig());
          return pageLoader.getCollection();
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<Build> getDependantBuilds(String id) throws RemoteResourceException {
    try {
      return getDependantBuilds(id, Optional.empty(), Optional.empty());
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getDependantBuilds(id, Optional.empty(), Optional.empty());
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<MilestoneInfo> getMilestonesInfo(String id) throws
      RemoteResourceException {
    try {
      PageReader pageLoader = new PageReader<>((pageParameters) -> {  return getEndpoint().getMilestonesInfo(id, pageParameters);}, getRemoteCollectionConfig());
      return pageLoader.getCollection();
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          PageReader pageLoader = new PageReader<>((pageParameters) -> {  return getEndpoint().getMilestonesInfo(id, pageParameters);}, getRemoteCollectionConfig());
          return pageLoader.getCollection();
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<ArtifactRevision> getRevisions(String id, Optional<String> sort,
      Optional<String> query) throws RemoteResourceException {
    try {
      PageReader pageLoader = new PageReader<>((pageParameters) -> { setSortAndQuery(pageParameters, sort, query); return getEndpoint().getRevisions(id, pageParameters);}, getRemoteCollectionConfig());
      return pageLoader.getCollection();
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          PageReader pageLoader = new PageReader<>((pageParameters) -> { setSortAndQuery(pageParameters, sort, query); return getEndpoint().getRevisions(id, pageParameters);}, getRemoteCollectionConfig());
          return pageLoader.getCollection();
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public RemoteCollection<ArtifactRevision> getRevisions(String id) throws RemoteResourceException {
    try {
      return getRevisions(id, Optional.empty(), Optional.empty());
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getRevisions(id, Optional.empty(), Optional.empty());
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }

  public ArtifactRevision getRevision(String id, int rev) throws RemoteResourceException {
    try {
      return getEndpoint().getRevision(id, rev);
    } catch (NotFoundException e) {
      throw new RemoteResourceNotFoundException(e);
    } catch (NotAuthorizedException e) {
      if (configuration.getBearerTokenSupplier() != null) {
        try {
          bearerAuthentication.setToken(configuration.getBearerTokenSupplier().get());
          return getEndpoint().getRevision(id, rev);
        } catch (WebApplicationException wae) {
          throw new RemoteResourceException(readErrorResponse(wae), wae);
        }
      } else {
        throw new RemoteResourceException(readErrorResponse(e), e);
      }
    } catch (WebApplicationException e) {
      throw new RemoteResourceException(readErrorResponse(e), e);
    }
  }
}
