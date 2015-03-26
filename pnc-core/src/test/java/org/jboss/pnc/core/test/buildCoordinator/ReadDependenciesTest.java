package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.core.builder.BuildSetTask;
import org.jboss.pnc.core.builder.BuildTasksTree;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class ReadDependenciesTest extends ProjectBuilder {

    @Test
    public void createDependencyTreeTestCase() {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();
        ContentIdentityManager contentIdentityManager = new ContentIdentityManager();
        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSet();
        BuildSetTask buildSetTask = new BuildSetTask(buildConfigurationSet);
        BuildTasksTree buildTasksTree = BuildTasksTree.newInstance(buildCoordinator, buildSetTask);

        BuildTasksTree buildTasksTree = new BuildTasksTree(buildCoordinator);
        BuildConfiguration buildConfiguration = configurationBuilder.buildConfigurationWithDependencies();
        BuildTask buildTask = buildTasksTree.getOrCreateSubmittedBuild(buildConfiguration);

        Assert.assertEquals("Missing projects in tree structure.", 5, buildTasksTree.getSubmittedBuilds().size());

    }
}
