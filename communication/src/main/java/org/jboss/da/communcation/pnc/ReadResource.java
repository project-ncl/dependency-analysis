package org.jboss.da.communcation.pnc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by dcheung on 22/06/15.
 */
public class ReadResource {

    public static String getResource(String key) {
        InputStream inputStream = ReadResource.class
                .getClassLoader()
                .getResourceAsStream("credentials.properties");
        try {
            Properties prop = new Properties();
            prop.load(inputStream);
            return prop.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
