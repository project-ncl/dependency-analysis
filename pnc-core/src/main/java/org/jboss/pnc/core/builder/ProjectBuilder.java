package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class ProjectBuilder {

    @Inject
    private BuildQueue buildQueue;

    @Inject
    private Logger log;

    public void buildProject(ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<Exception> onError) {
        try {
            buildQueue.add(new BuildTask(projectBuildConfiguration, onStatusUpdate, onError));
        } catch (Exception e) {
            onError.accept(e);
        }
    }

}
