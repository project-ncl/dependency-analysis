package org.jboss.da.reports.backend.impl;

import java.util.ArrayList;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.reports.backend.api.VersionFinder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.da.communication.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class VersionFinderImpl implements VersionFinder {

    Logger log = LoggerFactory.getLogger(VersionFinderImpl.class);

    @Inject
    private AproxConnector aproxConnector;

    @Override
    public List<String> getVersionsFor(GAV gav) {
        try {
            return aproxConnector.getVersionsOfGA(gavToGA(gav));
        } catch (CommunicationException ex) {
            log.error("Failed to get versions for " + gav, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public String getBestMatchVersionFor(GAV gav) {
        try {
            List<String> obtainedVersions = aproxConnector.getVersionsOfGA(gavToGA(gav));
            return findBiggestMatchingVersion(gav, obtainedVersions);
        } catch (CommunicationException ex) {
            log.error("Failed to get versions for " + gav, ex);
            return null;
        }
    }

    private String findBiggestMatchingVersion(GAV gav, List<String> obtainedVersions) {
        String bestMatchVersion = null;
        int biggestBuildNumber = 0;

        String origVersion = gav.getVersion();
        Pattern pattern = Pattern.compile(origVersion + ".*\\.redhat-(\\d+)\\D*");

        for (String ver : obtainedVersions) {
            Matcher matcher = pattern.matcher(ver);
            if (matcher.matches()) {
                int foundBuildNumber = Integer.parseInt(matcher.group(1));
                if (foundBuildNumber > biggestBuildNumber) {
                    bestMatchVersion = ver;
                    biggestBuildNumber = foundBuildNumber;
                }
            }
        }
        return bestMatchVersion;
    }

    private GA gavToGA(GAV gav) {
        return new GA(gav.getGroupId(), gav.getArtifactId());
    }

}
