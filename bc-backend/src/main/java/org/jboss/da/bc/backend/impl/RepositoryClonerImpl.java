package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.RepositoryCloner;
import org.jboss.da.bc.model.RepourPullResponse;
import org.jboss.da.bc.model.RepourRequest;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.util.HttpUtil;
import org.jboss.da.scm.api.SCMType;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Service to clone the repositories to the internal systems
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class RepositoryClonerImpl implements RepositoryCloner {

    private String repourBaseUrl;

    private String repourPullUrl;

    @Inject
    private Configuration configuration;

    @PostConstruct
    private void init() {
        try {
            repourBaseUrl = configuration.getConfig().getRepourUrl();
            repourPullUrl = repourBaseUrl + "/pull";
        } catch (ConfigurationParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String cloneRepository(String url, String revision, SCMType scmType,
            String repositoryName) throws Exception {
        if (scmType != SCMType.GIT)
            throw new UnsupportedOperationException(
                    "Currently it is possible to clone only GIT repositories.");

        RepourPullResponse response = HttpUtil.processPostRequest(RepourPullResponse.class,
                new RepourRequest(repositoryName, scmType.name().toLowerCase(), revision, url),
                repourPullUrl);

        return response.getUrl().getReadonly();
    }
}
