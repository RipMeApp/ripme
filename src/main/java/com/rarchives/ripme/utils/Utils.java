package com.rarchives.ripme.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Utils {

    public static final String RIP_DIRECTORY = "rips";
    private static final Logger logger = Logger.getLogger(Utils.class);

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
        String value = defaultValue;
        try {
            Configuration config = new PropertiesConfiguration("config/rip.properties");
            value = config.getString(key);
        } catch (ConfigurationException e) {
            logger.error("Failed to get configuration value for " + key
                    + ", using default '" + value + "'");
        }
        return value;
    }

    public static int getConfigInteger(String key, int defaultValue) {
        int value = defaultValue;
        try {
            Configuration config = new PropertiesConfiguration(new File("./config/rip.properties"));
            value = config.getInt(key, defaultValue);
        } catch (Exception e) {
            logger.error("Failed to get configuration value for " + key
                    + ", using default '" + value + "'");
        }
        return value;
    }

    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        boolean value = defaultValue;
        try {
            Configuration config = new PropertiesConfiguration(new File("./config/rip.properties"));
            value = config.getBoolean(key, defaultValue);
        } catch (Exception e) {
            logger.error("Failed to get configuration value for " + key
                    + ", using default '" + value + "'");
        }
        return value;
    }

}