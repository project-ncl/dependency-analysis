package org.jboss.da.lookup.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.jboss.da.model.rest.NPMPackage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NPMLookupResult {

    @NonNull
    @JsonUnwrapped
    private NPMPackage npmPackage;

    private String bestMatchVersion;
}
