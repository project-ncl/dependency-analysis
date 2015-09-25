package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.RepositoryCloner;
import org.jboss.da.bc.model.RepourPullResponse;
import org.jboss.da.bc.model.RepourRequest;
import org.jboss.da.common.util.HttpUtil;
import org.jboss.da.scm.SCMType;

import javax.enterprise.context.ApplicationScoped;

/**
 * Service to clone the repositories to the internal systems
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class RepositoryClonerImpl implements RepositoryCloner {

    private static final String REPOUR_BASE_URL = "http://ncl-test-vm-02.host.prod.eng.bos.redhat.com:7331";

    private static final String REPOUR_PULL_URL = REPOUR_BASE_URL + "/pull";

    @Override
    public String cloneRepository(String url, String revision, SCMType scmType,
            String repositoryName) throws Exception {
        if (scmType != SCMType.GIT)
            throw new UnsupportedOperationException(
                    "Currently it is possible to clone only GIT repositories.");

        RepourPullResponse response = HttpUtil.processPostRequest(RepourPullResponse.class,
                new RepourRequest(repositoryName, scmType.name().toLowerCase(), revision, url),
                REPOUR_PULL_URL);

        return response.getUrl().getReadwrite();
    }
}
