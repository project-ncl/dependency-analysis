package org.jboss.da.listings.model.rest;

import com.fasterxml.jackson.annotation.JsonRootName;

import org.jboss.da.listings.model.ProductSupportStatus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonRootName(value = "product")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RestProductInput {

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected ProductSupportStatus supportStatus;
}
