package org.jboss.da.listings.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.jboss.da.listings.model.ProductSupportStatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonRootName(value = "product")
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RestProduct {

    @Getter
    @Setter
    protected Long id;

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    @JsonProperty(required = false)
    protected ProductSupportStatus supportStatus;
}
