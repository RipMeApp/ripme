package com.rarchives.ripme.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class Utils {

    public static final String RIP_DIRECTORY = "rips";

    public static File getWorkingDirectory() throws IOException {
        String path = new File(".").getCanonicalPath() + File.separator;
        path += RIP_DIRECTORY + File.separator;
        File workingDir = new File(path);
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }
        return workingDir;
    }
    
    public static String getConfigString(String key) {
        Configuration config = null;
        try {
            config = new PropertiesConfiguration("rip.properties");
        } catch (ConfigurationException e) {
            System.err.println(e);
            return null;
        }
        return config.getString(key);
    }
    
    public static void downloadFile(String url, File saveAs) throws IOException {
        Response response = Jsoup.connect(url)
                                 .ignoreContentType(true)
                                 .execute();

        FileOutputStream out = (new FileOutputStream(saveAs));
        out.write(response.bodyAsBytes());
        out.close();
    }
}