package org.jboss.da.reports.api;

import java.util.Optional;

import org.jboss.da.model.rest.GAV;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactReportTest {

    @Test
    public void testGetNotBuiltDependencies() {
        ArtifactReport ar = new ArtifactReport(new GAV("otg.example", "example", "0.1"));

        assertEquals(0, ar.getNotBuiltDependencies());

        ArtifactReport ar1 = new ArtifactReport(new GAV("otg.example", "dep1", "0.1"));
        ar.addDependency(ar1);

        assertEquals(1, ar.getNotBuiltDependencies());

        ArtifactReport ar2 = new ArtifactReport(new GAV("otg.example", "dep2", "0.1"));
        ar2.setBestMatchVersion(Optional.of("0.1.0.redhat-2"));
        ar.addDependency(ar2);

        assertEquals(1, ar.getNotBuiltDependencies());

        ArtifactReport ar11 = new ArtifactReport(new GAV("otg.example", "dep11", "0.1"));
        ar1.addDependency(ar11);

        assertEquals(2, ar.getNotBuiltDependencies());

        ArtifactReport ar12 = new ArtifactReport(new GAV("otg.example", "dep12", "0.1"));
        ar12.setBestMatchVersion(Optional.of("0.1.0.redhat-2"));
        ar12.addDependency(ar12);

        assertEquals(2, ar.getNotBuiltDependencies());
    }
}
