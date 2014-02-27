package com.rarchives.ripme.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Utils {

    public static final String RIP_DIRECTORY = "rips";
    private static final File configFile = new File("src/main/resources/rip.properties");
    private static final Logger logger = Logger.getLogger(Utils.class);

    private static Configuration config;
    static {
        try {
            config = new PropertiesConfiguration(configFile);
        } catch (ConfigurationException e) {
            logger.error("Failed to load properties file from " + configFile, e);
        }
    }

    public static File getWorkingDirectory() throws IOException {
        String path = new File(".").getCanonicalPath() + File.separator;
        path += RIP_DIRECTORY + File.separator;
        File workingDir = new File(path);
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }
        return workingDir;
    }

    public static String getConfigString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static int getConfigInteger(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public static void setConfigBoolean(String key, boolean value) {
        config.setProperty(key, value);
    }

}