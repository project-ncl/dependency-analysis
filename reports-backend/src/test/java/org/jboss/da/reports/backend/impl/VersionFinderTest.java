package org.jboss.da.reports.backend.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.communication.aprox.model.GAV;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import org.jboss.da.communication.CommunicationException;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionFinderTest {

    private static final String[] BUILT_HIBERNATE_VERSIONS = { "1.0.0.redhat-1", "1.0.0.redhat-18",
            "1.0.20", "1.1.1.redhat-15", "1.1.5.redhat-3", "1.1.4.redhat-20", "1.1.5.redhat-5",
            "1.1.5.redhat-18", "1.1.5.redhat-16" };

    @Mock
    private AproxConnector aproxConnector;

    @InjectMocks
    @Spy
    private VersionFinderImpl versionFinderImpl;

    private List<String> builtHibernateVersions = Arrays.asList(BUILT_HIBERNATE_VERSIONS);

    @Test
    public void getBestMatchVersionForTest() throws CommunicationException {
        GA requestedGA = new GA("org.hibernate", "hibernate-core");
        System.err.println("\n\n\n " + builtHibernateVersions);
        when(aproxConnector.getVersionsOfGA(requestedGA)).thenReturn(builtHibernateVersions);

        GAV okGAV = new GAV("org.hibernate", "hibernate-core", "1.1.5");
        assertEquals("1.1.5.redhat-18", versionFinderImpl.getBestMatchVersionFor(okGAV));

        GAV ontBuiltGAV = new GAV("org.hibernate", "hibernate-core", "1.1.3");
        assertEquals(null, versionFinderImpl.getBestMatchVersionFor(ontBuiltGAV));
    }

}
