package org.jboss.da.rest.reports.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@RequiredArgsConstructor
public class Report {

    @Getter
    @NonNull
    @XmlElement(required = true, name = "group_id")
    private final String groupId;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "artifact_id")
    private final String artifactId;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "version")
    private final String version;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "available_versions")
    private final List<String> availableVersions;

    @Getter
    @XmlElement(required = false, name = "best_match_version")
    private final String bestMatchVersion;

    @Getter
    @XmlElement(required = false, name = "dependency_versions_satisfied")
    private final boolean dependencyVersionsSatisfied;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "dependencies")
    private final List<Report> dependencies;
}
