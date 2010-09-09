package org.apache.ibatis.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class ExternalResources {

    ExternalResources() {
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
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
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
