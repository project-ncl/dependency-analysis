/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.da.common.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.MDC;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class MDCUtils {

    private static final String REQUEST_CONTEXT_KEY = "requestContext";

    private static final String PROCESS_CONTEXT_KEY = "processContext";

    private static final String USER_ID_KEY = "userId";

    private static final String TMP_KEY = "tmp";;

    private static final String EXP_KEY = "exp";

    private static final Map<String, String> MDC_TO_HEADER = new HashMap<>();

    private static final Map<String, String> HEADER_TO_MDC = new HashMap<>();

    static {
        MDC_TO_HEADER.put(USER_ID_KEY, "log-user-id");
        MDC_TO_HEADER.put(REQUEST_CONTEXT_KEY, "log-request-context");
        MDC_TO_HEADER.put(PROCESS_CONTEXT_KEY, "log-process-context");
        MDC_TO_HEADER.put(TMP_KEY, "log-tmp");
        MDC_TO_HEADER.put(EXP_KEY, "log-exp");
        MDC_TO_HEADER.forEach((k, v) -> HEADER_TO_MDC.put(v, k));
    }

    public static Map<String, String> headersFromContext() {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> e : MDC_TO_HEADER.entrySet()) {
            String value = MDC.get(e.getKey());
            if (value != null) {
                headers.put(e.getValue(), value);
            }
        }
        return headers;
    }

    public static void contextFromHeader(String header, String value) {
        MDC.put(HEADER_TO_MDC.get(header), value);
    }

    public static Set<String> keys() {
        return MDC_TO_HEADER.keySet();
    }

    public static Set<String> headerKeys() {
        return HEADER_TO_MDC.keySet();
    }
}
