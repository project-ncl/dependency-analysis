package org.jboss.da.scm.impl;

import org.jboss.da.scm.api.SCMType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
class SCMSpecifier {

    @Getter
    private final SCMType scmType;

    @Getter
    private final String scmUrl;

    @Getter
    private final String revision;

}
