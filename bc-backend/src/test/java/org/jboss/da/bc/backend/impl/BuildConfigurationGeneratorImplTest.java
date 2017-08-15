package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.impl.BuildConfigurationGeneratorImpl;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.backend.ProductGeneratorEntity;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.model.api.SCMLocator;
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
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
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
        ProductGeneratorEntity genEntity = prepareGeneratorEntity("testName:1");
        bcGenerator.createBC(genEntity);
        assertEquals(genEntity.getToplevelBc().getProject().getErrors().size(), 1);
        assertTrue(genEntity.getToplevelBc().getProject().getErrors().contains(BcError.NO_NAME));
    }

    @Test
    public void testValidBcName() throws Exception {
        ProductGeneratorEntity genEntity = prepareGeneratorEntity("testName-1_0");
        when(
                finalizer.createBCs(genEntity.getId(), genEntity.getProductVersion(),
                        genEntity.getToplevelBc(), genEntity.getBcSetName(), null)).thenReturn(1);

        assertEquals(Integer.valueOf(1), bcGenerator.createBC(genEntity).get());
    }

    private ProductGeneratorEntity prepareGeneratorEntity(String bcName) {
        ProjectDetail singleProject = new ProjectDetail(null);
        singleProject.setEnvironmentId(1);
        singleProject.setProjectId(1);
        singleProject.setName(bcName);
        singleProject.setSCM("https://github.com/example/test.git", "master");
        ProjectHiearchy projectHierarchy = new ProjectHiearchy(singleProject, false);
        projectHierarchy.setSelected(true);

        ProductGeneratorEntity genEntity = new ProductGeneratorEntity(SCMLocator.internal("scm",
                "rev", "path"), 0, new GAV("G", "A", "V"), "version");
        genEntity.setBcSetName("BCSetName");
        genEntity.setProductVersion("1.1");
        genEntity.setId(0);
        genEntity.setToplevelBc(projectHierarchy);
        return genEntity;
    }
}
