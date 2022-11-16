package org.jboss.da.rest;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.lookup.model.MavenLatestRequest;
import org.jboss.da.lookup.model.MavenLatestResult;
import org.jboss.da.lookup.model.MavenLookupRequest;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.MavenVersionsRequest;
import org.jboss.da.lookup.model.MavenVersionsResult;
import org.jboss.da.lookup.model.NPMLookupRequest;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.lookup.model.NPMVersionsRequest;
import org.jboss.da.lookup.model.NPMVersionsResult;
import org.jboss.da.reports.api.LookupGenerator;
import org.jboss.da.rest.api.Lookup;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class LookupImpl implements Lookup {

    @Inject
    private LookupGenerator lookupGenerator;

    @Override
    @TimedMetric
    @WithSpan()
    public Set<MavenLookupResult> lookupMaven(@SpanAttribute(value = "request") MavenLookupRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/maven. Payload: " + request.toString());
        Set<MavenLookupResult> result = lookupGenerator
                .lookupBestMatchMaven(request.getArtifacts(), request.getMode(), request.isBrewPullActive());
        log.info("Request to /lookup/maven completed successfully. Payload: " + request.toString());
        return result;
    }

    @Override
    @WithSpan()
    public Set<MavenVersionsResult> versionsMaven(@SpanAttribute(value = "request") MavenVersionsRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/maven/versions. Payload: " + request.toString());
        Set<MavenVersionsResult> result = lookupGenerator.lookupVersionsMaven(
                request.getArtifacts(),
                request.getFilter(),
                request.getMode(),
                request.isBrewPullActive(),
                request.isIncludeBad());
        log.info("Request to /lookup/maven/versions completed successfully. Payload: " + request.toString());
        return result;
    }

    @Override
    @WithSpan()
    public Set<MavenLatestResult> lookupMaven(@SpanAttribute(value = "request") MavenLatestRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/maven/latest. Payload: " + request);
        Set<MavenLatestResult> result = lookupGenerator.lookupLatestMaven(request.getArtifacts(), request.getMode());
        log.info("Request to /lookup/maven/latest completed successfully. Payload: " + request);
        return result;
    }

    @Override
    @TimedMetric
    @WithSpan()
    public Set<NPMLookupResult> lookupNPM(@SpanAttribute(value = "request") NPMLookupRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/npm. Payload: " + request.toString());
        Set<NPMLookupResult> result = lookupGenerator.lookupBestMatchNPM(request.getPackages(), request.getMode());
        log.info("Request to /lookup/npm completed successfully. Payload: " + request.toString());
        return result;
    }

    @Override
    @WithSpan()
    public Set<NPMVersionsResult> versionsNPM(@SpanAttribute(value = "request") NPMVersionsRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/npm/versions. Payload: " + request.toString());
        Set<NPMVersionsResult> result = lookupGenerator.lookupVersionsNPM(
                request.getPackages(),
                request.getFilter(),
                request.getMode(),
                request.isIncludeBad());
        log.info("Request to /lookup/npm/versions completed successfully. Payload: " + request.toString());
        return result;
    }
}
