package com.rarchives.ripme.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rarchives.ripme.ripper.AbstractRipper;

/**
 * Common utility functions used in various places throughout the project.
 */
public class Utils {

    public  static final String RIP_DIRECTORY = "rips";
    private static final String configFile = "rip.properties";
    private static final Logger logger = Logger.getLogger(Utils.class);

    private static PropertiesConfiguration config;
    static {
        try {
            String configPath = getConfigPath();
            File f = new File(configPath);
            if (!f.exists()) {
                // Use default bundled with .jar
                configPath = configFile;
            }
            config = new PropertiesConfiguration(configPath);
            logger.info("Loaded " + config.getPath());
            if (f.exists()){
                // Config was loaded from file
                if ( !config.containsKey("twitter.auth")
                  || !config.containsKey("twitter.max_requests")
                  || !config.containsKey("tumblr.auth")
                  || !config.containsKey("error.skip404")
                  || !config.containsKey("gw.api")
                  || !config.containsKey("page.timeout")
                  || !config.containsKey("download.max_size")
                  ) {
                    // Config is missing key fields
                    // Need to reload the default config
                    // See https://github.com/4pr0n/ripme/issues/158
                    logger.warn("Config does not contain key fields, deleting old config");
                    f.delete();
                    config = new PropertiesConfiguration(configFile);
                    logger.info("Loaded " + config.getPath());
                }
            }
        } catch (Exception e) {
            logger.error("[!] Failed to load properties file from " + configFile, e);
        }
    }

    /**
     * Get the root rips directory.
     * @return
     *      Root directory to save rips to.
     * @throws IOException
     */
    public static File getWorkingDirectory() {
        String currentDir = ".";
        try {
            currentDir = new File(".").getCanonicalPath() + File.separator + RIP_DIRECTORY + File.separator;
        } catch (IOException e) {
            logger.error("Error while finding working dir: ", e);
        }
        if (config != null) {
            currentDir = getConfigString("rips.directory", currentDir);
        }
        File workingDir = new File(currentDir);
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
    public static List<String> getConfigList(String key) {
        List<String> result = new ArrayList<String>();
        for (Object obj : config.getList(key, new ArrayList<String>())) {
            if (obj instanceof String) {
                result.add( (String) obj);
            }
        }
        return result;
    }
    public static void setConfigBoolean(String key, boolean value)  { config.setProperty(key, value); }
    public static void setConfigString(String key, String value)    { config.setProperty(key, value); }
    public static void setConfigInteger(String key, int value)      { config.setProperty(key, value); }
    public static void setConfigList(String key, List<Object> list) {
        config.clearProperty(key);
        config.addProperty(key, list);
    }
    public static void setConfigList(String key, Enumeration<Object> enumeration) {
        config.clearProperty(key);
        List<Object> list = new ArrayList<Object>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        config.addProperty(key, list);
    }

    public static void saveConfig() {
        try {
            config.save(getConfigPath());
            logger.info("Saved configuration to " + getConfigPath());
        } catch (ConfigurationException e) {
            logger.error("Error while saving configuration: ", e);
        }
    }
    private static String getConfigPath() {
        try {
            return new File(".").getCanonicalPath() + File.separator + configFile;
        } catch (Exception e) {
            return "." + File.separator + configFile;
        }
    }

    /**
     * Removes the current working directory (CWD) from a File.
     * @param saveAs
     *      The File path
     * @return
     *      saveAs in relation to the CWD
     */
    public static String removeCWD(File saveAs) {
        String prettySaveAs = saveAs.toString(); 
        try {
            prettySaveAs = saveAs.getCanonicalPath();
            String cwd = new File(".").getCanonicalPath() + File.separator;
            prettySaveAs = prettySaveAs.replace(
                    cwd,
                    "." + File.separator);
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return prettySaveAs;
    }
    
    public static String stripURLParameter(String url, String parameter) {
        int paramIndex = url.indexOf("?" + parameter);
        boolean wasFirstParam = true;
        if(paramIndex < 0) {
            wasFirstParam = false;
            paramIndex = url.indexOf("&" + parameter);
        }
        
        if(paramIndex > 0) {
            int nextParam = url.indexOf("&", paramIndex+1);
            if(nextParam != -1) {
                String c = "&";
                if(wasFirstParam) c = "?";
                url = url.substring(0, paramIndex) + c + url.substring(nextParam+1, url.length());
            } else {
                url = url.substring(0, paramIndex);
            }
        }
        
        return url;
    }

    /**
     * Removes the current working directory from a given filename
     * @param file
     * @return
     *      'file' without the leading current working directory
     */
    public static String removeCWD(String file) {
        return removeCWD(new File(file));
    }

    /**
     * Get a list of all Classes within a package.
     * Works with file system projects and jar files!
     * Borrowed from StackOverflow, but I don't have a link :[
     * @param pkgname
     *      The name of the package
     * @return
     *      List of classes within the package
     */
    public static ArrayList<Class<?>> getClassesForPackage(String pkgname) {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        String relPath = pkgname.replace('.', '/');
        URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
        if (resource == null) {
            throw new RuntimeException("No resource for " + relPath);
        }

        String fullPath = resource.getFile();
        File directory = null;
        try {
            directory = new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(pkgname + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
        } catch (IllegalArgumentException e) {
            directory = null;
        }

        if (directory != null && directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class") && !file.contains("$")) {
                    String className = pkgname + '.' + file.substring(0, file.length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("ClassNotFoundException loading " + className);
                    }
                }
            }
        }
        else {
            // Load from JAR
            try {
                String jarPath = fullPath
                        .replaceFirst("[.]jar[!].*", ".jar")
                        .replaceFirst("file:", "");
                jarPath = URLDecoder.decode(jarPath, "UTF-8");
                JarFile jarFile = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jarFile.entries();
                while(entries.hasMoreElements()) {
                    JarEntry nextElement = entries.nextElement();
                    String entryName = nextElement.getName();
                    if(entryName.startsWith(relPath)
                            && entryName.length() > (relPath.length() + "/".length())
                            && !nextElement.isDirectory()) {
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            logger.error("ClassNotFoundException loading " + className);
                            jarFile.close(); // Resource leak fix?
                            throw new RuntimeException("ClassNotFoundException loading " + className);
                        }
                    }
                }
                jarFile.close(); // Eclipse said not closing it would have a resource leak
            } catch (IOException e) {
                logger.error("Error while loading jar file:", e);
                throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
            }
        }
        return classes;
    }
    
    public static final int SHORTENED_PATH_LENGTH = 12;
    public static String shortenPath(String path) {
        return shortenPath(new File(path));
    }
    public static String shortenPath(File file) {
        String path = removeCWD(file);
        if (path.length() < SHORTENED_PATH_LENGTH * 2) {
            return path;
        }
        return path.substring(0, SHORTENED_PATH_LENGTH)
                + "..."
                + path.substring(path.length() - SHORTENED_PATH_LENGTH);
    }
    
    public static String filesystemSafe(String text) {
        text = text.replaceAll("[^a-zA-Z0-9.-]", "_")
                   .replaceAll("__", "_")
                   .replaceAll("_+$", "");
        if (text.length() > 100) {
            text = text.substring(0, 99);
        }
        return text;
    }
    
    public static String bytesToHumanReadable(int bytes) {
        float fbytes = (float) bytes;
        String[] mags = new String[] {"", "k", "m", "g", "t"};
        int magIndex = 0;
        while (fbytes >= 1024) {
            fbytes /= 1024;
            magIndex++;
        }
        return String.format("%.2f%sb", fbytes, mags[magIndex]);
    }

    public static List<String> getListOfAlbumRippers() throws Exception {
        List<String> list = new ArrayList<String>();
        for (Constructor<?> ripper : AbstractRipper.getRipperConstructors("com.rarchives.ripme.ripper.rippers")) {
            list.add(ripper.getName());
        }
        return list;
    }
    public static List<String> getListOfVideoRippers() throws Exception {
        List<String> list = new ArrayList<String>();
        for (Constructor<?> ripper : AbstractRipper.getRipperConstructors("com.rarchives.ripme.ripper.rippers.video")) {
            list.add(ripper.getName());
        }
        return list;
    }

    public static void playSound(String filename) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(filename);
        try {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }
            });
            clip.open(AudioSystem.getAudioInputStream(resource));
            clip.start();
        } catch (Exception e) {
            logger.error("Failed to play sound " + filename, e);
        }
    }

    /**
     * Configures root logger, either for FILE output or just console.
     */
    public static void configureLogger() {
        LogManager.shutdown();
        String logFile;
        if (getConfigBoolean("log.save", false)) {
            logFile = "log4j.file.properties";
        }
        else {
            logFile = "log4j.properties";
        }
        InputStream stream = Utils.class.getClassLoader().getResourceAsStream(logFile);
        if (stream == null) {
            PropertyConfigurator.configure("src/main/resources/" + logFile);
        } else {
            PropertyConfigurator.configure(stream);
        }
        logger.info("Loaded " + logFile);
        try {
            stream.close();
        } catch (IOException e) { }
    }

    /**
     * Gets list of strings between two strings.
     * @param fullText Text to retrieve from.
     * @param start String that precedes the desired text
     * @param finish String that follows the desired text
     * @return List of all strings that are between 'start' and 'finish'
     */
    public static List<String> between(String fullText, String start, String finish) {
        List<String> result = new ArrayList<String>();
        int i, j;
        i = fullText.indexOf(start);
        while (i >= 0) {
            i += start.length();
            j = fullText.indexOf(finish, i);
            if (j < 0) {
                break;
            }
            result.add(fullText.substring(i, j));
            i = fullText.indexOf(start, j + finish.length());
        }
        return result;
    }
}