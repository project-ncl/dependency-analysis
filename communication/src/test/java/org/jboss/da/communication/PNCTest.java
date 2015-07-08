package org.jboss.da.communication;

import org.jboss.da.communcation.pnc.PNC;
import org.jboss.da.communcation.pnc.PNCProducer;
import org.junit.Test;
import static org.junit.Assert.*;

public class PNCTest {

    @Test
    public void pncTest() {
        PNCProducer producer = new PNCProducer();
        PNC pnc = producer.getPNCInstance();
        assertNotNull(pnc);
    }
}
