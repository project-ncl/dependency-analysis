package org.jboss.da.test.client.ws.reports;

import org.json.JSONException;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

public class WebsocketApiReportsTest extends AbstractWebsocketReportsTest {

    private static final String PATH_REPORTS_ALIGN = "/reports/align";

    private static final String METHOD_REPORTS_ALIGN = "reports.align";

    private static final String PATH_REPORTS_BUILT = "/reports/built";

    private static final String METHOD_REPORTS_BUILT = "reports.built";

    private static final String PATH_REPORTS_LOOKUP = "/reports/lookup/gavs";

    private static final String METHOD_REPORTS_LOOKUP = "reports.lookup.gav";

    @Test
    public void testAlignReportBasic() throws IOException, Exception {
        assertResponseForRequest(PATH_REPORTS_ALIGN, "dependency-analysis", METHOD_REPORTS_ALIGN);
    }

    @Test
    public void testBuiltReportBasic() throws IOException, Exception {
        assertResponseForRequest(PATH_REPORTS_BUILT, "dependency-analysis", METHOD_REPORTS_BUILT);
    }

    @Test
    public void testLookupReportBasic() throws IOException, Exception {
        assertResponseForRequest(PATH_REPORTS_LOOKUP, "guava13", METHOD_REPORTS_LOOKUP);
    }

    @Override
    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }
}
