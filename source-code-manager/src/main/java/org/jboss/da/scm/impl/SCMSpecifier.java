package org.jboss.da.scm.impl;

import org.jboss.da.scm.api.SCMType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class SCMSpecifier {

    @Getter
    private final SCMType scmType;

    @Getter
    private final String scmUrl;

    @Getter
    private final String revision;

}
