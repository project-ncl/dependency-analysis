/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.api.endpoints;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.annotation.RespondWithStatus;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.parameters.PaginationParameters;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface ArtifactEndpoint21 {
    static final String A_ID = "ID of the artifact";
    static final String A_REV = "Revision number of the artifact";

    static final String GET_ALL_DESC = "Gets all artifacts.";
    static final String FILTER_SHA256_DESC = "Filter by sha256 of the artifact.";
    static final String FILTER_SHA1_DESC = "Filter by sha1 of the artifact.";
    static final String FILTER_MD5_DESC = "Filter by md5 of the artifact.";

    /**
     * {@value GET_ALL_DESC}
     *
     * @param pageParams
     * @param sha256 {@value FILTER_SHA256_DESC}
     * @param md5 {@value FILTER_MD5_DESC}
     * @param sha1 {@value FILTER_SHA1_DESC}
     * @return
     */
    @GET
    Page<Artifact> getAll(
            @Valid @BeanParam PageParameters pageParams,
            @QueryParam("sha256") String sha256,
            @QueryParam("md5") String md5,
            @QueryParam("sha1") String sha1);

    static final String GET_ALL_FILTERED_DESC = "Gets all artifacts according to specified filters.";
    static final String FILTER_IDENTIFIER_DESC = "Filter by artifact identifier or its part.";
    static final String FILTER_QUALITY_DESC = "List of artifact qualities to include in result.";
    static final String FILTER_BUILD_CATEGORY_DESC = "List of artifact build categories to include in result.";
    static final String FILTER_REPOSITORY_TYPE_DESC = "Type of target repository.";

    /**
     * {@value GET_ALL_FILTERED_DESC}
     *
     * @param paginationParameters
     * @param identifier {@value FILTER_IDENTIFIER_DESC}
     * @param qualities {@value FILTER_QUALITY_DESC}
     * @param repoType {@value FILTER_REPOSITORY_TYPE_DESC}
     * @return
     */
    @GET
    @Path("/filter")
    Page<ArtifactInfo> getAllFiltered(
            @Valid @BeanParam PaginationParameters paginationParameters,
            @QueryParam("identifier") String identifier,
            @QueryParam("qualities") Set<ArtifactQuality> qualities,
            @QueryParam("repoType") RepositoryType repoType,
            @QueryParam("buildCategories") Set<BuildCategory> buildCategories);

    static final String GET_SPECIFIC_DESC = "Gets a specific build config.";

    /**
     * {@value GET_SPECIFIC_DESC}
     *
     * @param id {@value A_ID}
     * @return
     */
    @GET
    @Path("/{id}")
    Artifact getSpecific(@PathParam("id") String id);

    static final String CREATE_DESC = "Creates a new Artifact.";

    /**
     * {@value CREATE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param artifact
     * @return
     */
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    Artifact create(@NotNull Artifact artifact);

    static final String UPDATE_DESC = "Updates an existing Artifact";

    /**
     * {@value UPDATE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     *
     * @param id {@value A_ID}
     * @param artifact
     */
    @PUT
    @Path("/{id}")
    void update(@PathParam("id") String id, @NotNull Artifact artifact);

    static final String CREATE_ARTIFACT_QUALITY_REVISION = "Add a new quality level revision for this artifact. Accepted values from standard users are NEW, VERIFIED, TESTED, DEPRECATED. Users with system-user role can also specify BLACKLISTED and DELETED quality levels.";
    static final String ARTIFACT_QUALITY = "Quality level of the artifact.";
    static final String ARTIFACT_QUALITY_REASON = "The reason for adding a new quality level for this artifact.";

    /**
     * {@value CREATE_ARTIFACT_QUALITY_REVISION}
     *
     * @param id {@value A_ID}
     * @param quality {@value ARTIFACT_QUALITY}
     * @param reason {@value ARTIFACT_QUALITY_REASON}
     */
    @POST
    @RespondWithStatus(Response.Status.CREATED)
    @Path("/{id}/artifacts/quality")
    ArtifactRevision createQualityLevelRevision(
            @PathParam("id") String id,
            @QueryParam("quality") String quality,
            @QueryParam("reason") String reason);

    static final String GET_DEPENDANT_BUILDS_DESC = "Gets the build(s) that depends on this artifact.";

    /**
     * {@value GET_DEPENDANT_BUILDS_DESC}
     *
     * @param id {@value A_ID}
     * @param pageParams
     * @return
     */
    @GET
    @Path("/{id}/dependant-builds")
    Page<Build> getDependantBuilds(
            @PathParam("id") String id,
            @BeanParam PageParameters pageParams);

    static final String GET_MILESTONES_INFO_DESC = "Gets the milestones that produced or consumed this artifact.";

    /**
     * {@value GET_MILESTONES_INFO_DESC}
     *
     * @param id {@value A_ID}
     * @param pageParams
     * @return
     */
    @GET
    @Path("/{id}/milestones")
    Page<MilestoneInfo> getMilestonesInfo(
            @PathParam("id") String id,
            @BeanParam PaginationParameters pageParams);

    static final String GET_ARTIFACT_REVISIONS_DESC = "Gets audited revisions of this artifact.";

    /**
     * {@value GET_ARTIFACT_REVISIONS_DESC}
     *
     * @param id {@value A_ID}
     * @param pageParams
     * @return
     */
    @GET
    @Path("/{id}/revisions")
    Page<ArtifactRevision> getRevisions(
            @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParams);

    static final String GET_ARTIFACT_REVISION_DESC = "Get specific audited revision of this artifact.";

    /**
     * {@value GET_ARTIFACT_REVISION_DESC}
     *
     * @param id {@value A_ID}
     * @param rev {@value A_REV}
     * @return
     */
    @GET
    @Path("/{id}/revisions/{rev}")
    ArtifactRevision getRevision(
            @PathParam("id") String id,
            @PathParam("rev") int rev);
}
