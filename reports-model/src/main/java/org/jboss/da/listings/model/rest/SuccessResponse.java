package org.jboss.da.listings.model.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@JsonRootName(value = "success")
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {

    @Getter
    @Setter
    @JsonProperty(required = true, value = "success")
    protected boolean success;

    @Getter
    @Setter
    protected Long id;

    @Getter
    @Setter
    @JsonProperty(required = false, value = "message")
    protected String message;

}
