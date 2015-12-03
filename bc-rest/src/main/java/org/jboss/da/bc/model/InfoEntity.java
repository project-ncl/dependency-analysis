package org.jboss.da.bc.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@EqualsAndHashCode
public class InfoEntity {

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    protected BuildConfiguration topLevelBc;

    @Getter
    @Setter
    protected String bcSetName;

}
