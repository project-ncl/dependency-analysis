package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.impl.BuildConfigurationGeneratorImpl;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.reports.api.SCMLocator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildConfigurationGeneratorImplTest {

    @Mock
    private Finalizer finalizer;

    @Spy
    @InjectMocks
    private BuildConfigurationGeneratorImpl bcGenerator;

    @Test
    public void testInvalidBcName() throws Exception {
        GeneratorEntity genEntity = prepareGeneratorEntity("testName:1");
        bcGenerator.createBC(genEntity);
        assertEquals(genEntity.getToplevelBc().getProject().getErrors().size(), 1);
        assertTrue(genEntity.getToplevelBc().getProject().getErrors().contains(BcError.NO_NAME));
    }

    @Test
    public void testValidBcName() throws Exception {
        GeneratorEntity genEntity = prepareGeneratorEntity("testName-1_0");
        when(
                finalizer.createBCs(genEntity.getName(), genEntity.getProductVersion(),
                        genEntity.getToplevelBc(), genEntity.getBcSetName())).thenReturn(1);

        assertEquals(new Integer(1), bcGenerator.createBC(genEntity).get());
    }

    private GeneratorEntity prepareGeneratorEntity(String bcName) {
        ProjectDetail singleProject = new ProjectDetail(null);
        singleProject.setEnvironmentId(1);
        singleProject.setProjectId(1);
        singleProject.setName(bcName);
        ProjectHiearchy projectHierarchy = new ProjectHiearchy(singleProject, false);
        projectHierarchy.setSelected(true);

        GeneratorEntity genEntity = new GeneratorEntity(new SCMLocator("", "", ""), null, null,
                null);
        genEntity.setBcSetName("BCSetName");
        genEntity.setProductVersion("1.1");
        genEntity.setName("ProductName");
        genEntity.setToplevelBc(projectHierarchy);
        return genEntity;
    }
}
