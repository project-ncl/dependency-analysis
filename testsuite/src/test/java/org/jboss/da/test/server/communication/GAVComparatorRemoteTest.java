package org.jboss.da.test.server.communication;

import org.jboss.da.model.rest.GAV;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Stanislav Knot &lt;sknot@redhat.com&gt;
 */
public class GAVComparatorRemoteTest {

    @Test
    public void test() {
        // identical strings
        GAV gav1 = new GAV("com.megginson.sax", "xml-writer", "0.2");
        GAV gav2 = new GAV("com.megginson.sax", "xml-writer", "0.2");
        assertTrue(gav1.compareTo(gav2) == 0);

        // first string is less
        gav1 = new GAV("com.megginson.sax", "xml-writer", "0.2");
        gav2 = new GAV("dom.megginson.sax", "xml-writer", "0.2");
        assertTrue(gav1.compareTo(gav2) < 0);

        // first string is greater
        gav1 = new GAV("com.megginson.sax", "xml-writer", "0.2");
        gav2 = new GAV("aom.megginson.sax", "xml-writer", "0.2");
        assertTrue(gav1.compareTo(gav2) > 0);

        // first string is greater
        gav1 = new GAV("junit", "junit", "3.8.1");
        gav2 = new GAV("com.thoughtworks.xstream", "xstream", "1.4.2");
        assertTrue(gav1.compareTo(gav2) > 0);

        // second string is greater
        gav1 = new GAV("com.thoughtworks.xstream", "xstream", "1.4.2");
        gav2 = new GAV("junit", "junit", "3.8.1");
        assertFalse(gav1.compareTo(gav2) > 0);

    }

    @Test
    public void test2() {
        GAV gav1 = new GAV("javax.el", "javax.el-api", "3.0.0");
        GAV gav2 = new GAV("javax.inject", "javax.inject", "1");
        GAV gav3 = new GAV("javax.interceptor", "javax.interceptor-api", "1.2");
        GAV gav4 = new GAV("org.testng", "testng", "5.10");
        GAV gav5 = new GAV("javax.ws.rs", "javax.ws.rs-api", "2.0");

        assertTrue(gav1.compareTo(gav2) < 0);
        assertTrue(gav2.compareTo(gav3) < 0);
        assertTrue(gav3.compareTo(gav4) < 0);
        assertTrue(gav4.compareTo(gav5) > 0);

    }

}
