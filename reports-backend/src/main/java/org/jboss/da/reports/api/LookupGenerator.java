package org.jboss.da.reports.api;

import java.util.Set;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.lookup.model.MavenLatestResult;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.MavenVersionsResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.lookup.model.NPMVersionsResult;
import org.jboss.pnc.api.constants.versions.VersionDistanceRule;
import org.jboss.pnc.api.constants.versions.VersionFilter;
import org.jboss.da.model.rest.Strategy;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;

public interface LookupGenerator {

    Set<MavenLookupResult> lookupBestMatchMaven(
            Set<GAV> gavs,
            String mode,
            boolean brewPullActive,
            Set<Strategy> strategies) throws CommunicationException;

    Set<MavenVersionsResult> lookupVersionsMaven(
            Set<GAV> gavs,
            VersionFilter vf,
            VersionDistanceRule distanceRule,
            String mode,
            boolean brewPullActive,
            boolean includeBad) throws CommunicationException;

    Set<MavenLatestResult> lookupLatestMaven(Set<GAV> gavs, String mode) throws CommunicationException;

    Set<NPMLookupResult> lookupBestMatchNPM(Set<NPMPackage> packages, String mode) throws CommunicationException;

    Set<NPMVersionsResult> lookupVersionsNPM(
            Set<NPMPackage> packages,
            VersionFilter vf,
            VersionDistanceRule distanceRule,
            String mode,
            boolean includeBad) throws CommunicationException;
}
