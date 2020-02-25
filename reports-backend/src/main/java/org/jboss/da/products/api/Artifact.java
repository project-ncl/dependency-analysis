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
package org.jboss.da.products.api;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface Artifact {

    /**
     * Artifact identifier that identifies specific artifact version. Artifiact identifier consists of artifact name and version.
     *
     * @return artifact identifier
     */
    String getIdentifier();

    /**
     * Name of the artifact. It's usually identifier without version.
     *
     * @return artifact name.
     */
    String getName();

    /**
     * Version of the artifact.
     *
     * @return artifact version.
     */
    String getVersion();

    /**
     * Type of the artifact.
     * 
     * @return artifact type.
     */
    ArtifactType getType();
}
