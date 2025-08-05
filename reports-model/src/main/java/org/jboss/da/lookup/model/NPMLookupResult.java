package org.jboss.da.lookup.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NPMLookupResult extends NPMResult {

    private String bestMatchVersion;

    public NPMLookupResult(@NonNull NPMPackage npmPackage, String bestMatchVersion) {
        super(npmPackage);
        this.bestMatchVersion = bestMatchVersion;
    }
}
