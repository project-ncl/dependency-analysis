package org.jboss.da.common.util;

import java.io.File;

/**
 * Library class, which helps with the common file operations
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class FileUtils {

    /**
     * Deletes directory and its content
     */
    public static void deleteDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                deleteDirectory(file);
            else
                file.delete();
        }
        directory.delete();
    }
}
