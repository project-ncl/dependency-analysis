package org.jboss.da.bc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ToString
public class GeneratorEntity {

    @Getter
    @Setter
    String name;

    @Getter
    @Setter
    String bcSetName;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    ProjectHiearchy toplevelBc;

    @Getter
    @Setter
    String productVersion;

    public GeneratorEntity(SCMLocator scm, String name, GAV gav, String productVersion) {
        ProjectDetail pd = new ProjectDetail(gav);
        pd.setScmUrl(scm.getScmUrl());
        pd.setScmRevision(scm.getRevision());

        this.name = name;
        this.pomPath = scm.getPomPath();
        this.toplevelBc = new ProjectHiearchy(pd, true);
        this.productVersion = productVersion;
    }

    public ProjectDetail getToplevelProject() {
        return toplevelBc.getProject();
    }
}
