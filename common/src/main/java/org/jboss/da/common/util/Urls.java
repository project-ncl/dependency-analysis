package org.jboss.da.common.util;

public final class Urls {

    private Urls() {
    }

    /** Indy base URLs are normalized without a trailing slash (matches previous URL handling in the DA stack). */
    public static String withoutTrailingSlash(String url) {
        if (url == null || !url.endsWith("/")) {
            return url;
        }
        return url.substring(0, url.length() - 1);
    }
}
