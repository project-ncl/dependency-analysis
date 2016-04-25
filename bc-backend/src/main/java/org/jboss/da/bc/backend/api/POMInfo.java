package org.jboss.da.bc.backend.api;

import java.util.Optional;

import lombok.Getter;

import org.jboss.da.model.rest.GAV;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class POMInfo {

    @Getter
    private final GAV gav;

    @Getter
    private final Optional<String> scmURL;

    @Getter
    private final Optional<String> scmRevision;

    @Getter
    private final Optional<String> name;

    public POMInfo(GAV gav) {
        this.gav = gav;
        this.scmURL = Optional.empty();
        this.scmRevision = Optional.empty();
        this.name = Optional.empty();
    }

    public POMInfo(GAV gav, String scmURL, String scmRevision, String name) {
        this.gav = gav;
        this.scmURL = Optional.ofNullable(scmURL);
        this.scmRevision = Optional.ofNullable(scmRevision);
        this.name = Optional.ofNullable(name);
    }

}
