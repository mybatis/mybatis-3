package org.apache.ibatis.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class ExternalResources {

    private ExternalResources() {
        // do nothing
    }

    public static void copyExternalResource(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            closeQuietly(source);
            closeQuietly(destination);
        }

    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // do nothing, close quietly
            }
        }
    }

    public static String getConfiguredTemplate(String templatePath, String templateProperty) {
        String templateName = "";
        Properties migrationProperties = new Properties();

        try {
           migrationProperties.load(new FileInputStream(templatePath));
           templateName = migrationProperties.getProperty(templateProperty);
        } catch (Exception e) {
             e.printStackTrace();
        }

        return templateName;
     }

}
