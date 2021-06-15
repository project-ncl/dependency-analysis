package org.jboss.da.reports.api;

import java.util.Set;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.lookup.model.MavenLatestResult;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;

public interface LookupGenerator {

    Set<MavenLookupResult> lookupBestMatchMaven(Set<GAV> gavs, String mode, boolean brewPullActive)
            throws CommunicationException;

    Set<MavenLatestResult> lookupLatestMaven(Set<GAV> gavs, String mode) throws CommunicationException;

    Set<NPMLookupResult> lookupBestMatchNPM(Set<NPMPackage> packages, String mode) throws CommunicationException;
}
