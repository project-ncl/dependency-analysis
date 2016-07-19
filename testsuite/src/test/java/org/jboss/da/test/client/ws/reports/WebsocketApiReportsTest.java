package org.jboss.da.test.client.ws.reports;

import org.junit.Test;

import java.io.IOException;

public class WebsocketApiReportsTest extends AbstractWebsocketReportsTest {

    private static String PATH_REPORTS_ALIGN = "/reports/align";

    private static String METHOD_REPORTS_ALIGN = "reports.align";

    private static String PATH_REPORTS_BUILT = "/reports/built";

    private static String METHOD_REPORTS_BUILT = "reports.built";

    @Test
    public void testAlignReportBasic() throws IOException, Exception {
        assertResponseForRequest(PATH_REPORTS_ALIGN, "dependency-analysis", METHOD_REPORTS_ALIGN);
    }

    @Test
    public void testBuiltReportBasic() throws IOException, Exception {
        assertResponseForRequest(PATH_REPORTS_BUILT, "dependency-analysis", METHOD_REPORTS_BUILT);
    }
}
