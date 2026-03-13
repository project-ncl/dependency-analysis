package org.jboss.da.lookup.model;

import org.jboss.da.model.rest.NPMPackage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NPMResult {

    @NonNull
    @JsonUnwrapped
    private NPMPackage npmPackage;

}
