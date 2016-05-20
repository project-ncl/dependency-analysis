package org.jboss.da.bc.model.rest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FinishResponse<T> {

    @Getter
    @Setter
    protected Boolean success = false;

    @Getter
    @Setter
    protected String errorType;

    @Getter
    @Setter
    protected String message;

    @Getter
    @Setter
    protected T entity;

    public abstract void setCreatedEntityId(Integer id);

}
