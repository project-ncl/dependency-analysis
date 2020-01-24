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
package org.jboss.da.common.version;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.commonjava.maven.ext.manip.impl.Version;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@EqualsAndHashCode(exclude = { "originalVersion" })
@Getter
public class SuffixedVersion implements Comparable<SuffixedVersion> {

    private final int major;

    private final int minor;

    private final int micro;

    private final String qualifier;

    private final String suffix;

    private final Integer suffixVersion;

    private final String originalVersion;

    public SuffixedVersion(int major, int minor, int micro, String qualifier, String originalVersion) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = Objects.requireNonNull(qualifier);
        this.suffix = null;
        this.suffixVersion = null;
        this.originalVersion = originalVersion;
    }

    public SuffixedVersion(int major, int minor, int micro, String qualifier, String suffix, int suffixVersion,
            String originalVersion) {
        if (suffix == null || suffix.isEmpty()) {
            throw new IllegalArgumentException("Suffix must be provided in this constructor.");
        }
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = Objects.requireNonNull(qualifier);
        this.suffix = Objects.requireNonNull(suffix);
        this.suffixVersion = suffixVersion;
        this.originalVersion = originalVersion;
    }

    public Optional<String> getSuffix() {
        return Optional.ofNullable(suffix);
    }

    public Optional<Integer> getSuffixVersion() {
        return Optional.ofNullable(suffixVersion);
    }

    public boolean isSuffixed() {
        return suffix != null;
    }

    @Override
    public int compareTo(SuffixedVersion other) {
        int r = Integer.compare(this.major, other.major);
        if (r != 0) {
            return r;
        }
        r = Integer.compare(this.minor, other.minor);
        if (r != 0) {
            return r;
        }
        r = Integer.compare(this.micro, other.micro);
        if (r != 0) {
            return r;
        }
        r = this.qualifier.compareToIgnoreCase(other.qualifier);
        if (r != 0) {
            return r;
        }
        if (this.suffix == null) {
            if (other.suffix == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (other.suffix == null) {
            return 1;
        }
        r = this.suffix.compareToIgnoreCase(other.suffix);
        if (r != 0) {
            return r;
        }
        r = Integer.compare(this.suffixVersion, other.suffixVersion);
        if (r != 0) {
            return r;
        }
        if (this.isOsgiVersion() && !other.isOsgiVersion()) {
            return 1;
        } else if (!this.isOsgiVersion() && other.isOsgiVersion()) {
            return -1;
        }
        return 0;
    }

    private boolean isOsgiVersion() {
        Version osgi = new Version(originalVersion);
        return originalVersion.equals(osgi.getOSGiVersionString());
    }

    public final String normalizedVesion() {
        String q = qualifier.isEmpty() ? "" : '.' + qualifier;
        String s = "";
        if (suffix != null && !suffix.isEmpty()) {
            s = q.isEmpty() ? "." : "-";
            s += suffix + '-' + suffixVersion;
        }
        return major + "." + minor + "." + micro + q + s;
    }

    public final String unsuffixedVesion() {
        String q = qualifier.isEmpty() ? "" : '.' + qualifier;
        return major + "." + minor + "." + micro + q;
    }

    @Override
    public String toString() {
        return normalizedVesion();
    }

}
