package org.jboss.da.model.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author jbrazdil
 */
@RunWith(MockitoJUnitRunner.class)
public class GAVTest {

    @Test
    public void testValidGA() {
        assertTrue((new GA("com.example", "some-example")).isValid());
        assertTrue((new GA("com.101tec", "zkclient")).isValid()); // technically invallid, but comonly used

        assertFalse((new GA("com.ex ample", "some-example")).isValid());
        assertFalse((new GA("com.example", "some example")).isValid());
        assertFalse((new GA("com.-example", "some-example")).isValid());
        assertFalse((new GA("com.ex/ample", "some-example")).isValid());
        assertFalse((new GA("com.example", "some/example")).isValid());
        assertFalse((new GA("com.exa\nmple", "some-example")).isValid());
        assertFalse((new GA("com.example", "some\nexample")).isValid());
        assertFalse((new GA("com.exašmple", "some-example")).isValid());
    }

}
