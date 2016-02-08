package org.jboss.da.bc.model;

import lombok.ToString;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ToString
public class ProjectGeneratorEntity extends GeneratorEntity {

    public ProjectGeneratorEntity(SCMLocator scm, int id, GAV gav) {
        super(scm, id, gav);
    }

    public static EntityConstructor<ProjectGeneratorEntity> getConstructor(){
        return (s, n, g) -> new ProjectGeneratorEntity(s, n, g);
    }
}
