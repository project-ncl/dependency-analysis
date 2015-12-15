package org.jboss.da.bc.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FinishResponse {

    @Getter
    @Setter
    protected Boolean success = false;

    @Getter
    @Setter
    protected String errorType;

    @Getter
    @Setter
    protected String message;

    public abstract void setCreatedEntityId(Integer id);

}
