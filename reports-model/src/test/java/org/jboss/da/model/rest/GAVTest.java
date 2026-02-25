package org.jboss.da.model.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 *
 * @author jbrazdil
 */
public class GAVTest {

    @Test
    public void testValidGA() {
        assertTrue((new GA("com.example", "some-example")).isValid());
        assertTrue((new GA("com.101tec", "zkclient")).isValid()); // technically invalid, but commonly used

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
