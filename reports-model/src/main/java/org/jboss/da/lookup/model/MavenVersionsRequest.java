/*
 * Copyright 2018 Honza Brázdil &lt;jbrazdil@redhat.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.da.lookup.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.jboss.da.model.rest.GAV;
import org.jboss.pnc.api.constants.versions.VersionDistanceRule;
import org.jboss.pnc.api.constants.versions.VersionFilter;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenVersionsRequest {

    @NotNull
    private final VersionFilter filter;

    @NotNull
    @lombok.Builder.Default
    private final VersionDistanceRule distanceRule = VersionDistanceRule.RECOMMENDED_REPLACEMENT;

    @NotNull
    private final String mode;

    @NonNull
    private final Set<GAV> artifacts;

    @lombok.Builder.Default
    private final boolean includeBad = false;

    @lombok.Builder.Default
    private final boolean brewPullActive = false;

}
