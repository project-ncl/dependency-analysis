package org.jboss.da.bc.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil &lt;janinko.g@gmail.com&gt;
 */
@EqualsAndHashCode
public class InfoEntity {

    @Getter
    @Setter
    protected int id;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    @Valid
    protected BuildConfiguration topLevelBc;

    @Getter
    @Setter
    protected String bcSetName;

    @Getter
    @Setter
    @JsonProperty(required = false)
    protected String securityToken;

}
