package com.rarchives.ripme.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.sound.sampled.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rarchives.ripme.ripper.AbstractRipper;

/**
 * Common utility functions used in various places throughout the project.
 */
public class Utils {
    private static final String RIP_DIRECTORY = "rips";
    private static final String configFile = "rip.properties";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final Logger logger = Logger.getLogger(Utils.class);

    private static PropertiesConfiguration config;
    static {
        String configPath = getConfigFilePath();
        File f = new File(configPath);
        if (!f.exists()) {
            // Use default bundled with .jar
            configPath = configFile;
        }
        try {
            config = new PropertiesConfiguration(configPath);
        } catch (ConfigurationException e) {
            logger.error("Error loading user configuration.", e);
        }
        logger.info("Loaded " + config.getPath());
        if (f.exists()) {
            // Config was loaded from file
            if (!config.containsKey("twitter.auth")
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
                try {
                    config = new PropertiesConfiguration(configFile);
                } catch (ConfigurationException e) {
                    logger.error("Error loading default configutation file.", e);
                    System.exit(-1);
                }
                logger.info("Loaded " + config.getPath());
            }
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
        List<String> result = new ArrayList<>();
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
        List<Object> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        config.addProperty(key, list);
    }

    public static void saveConfig() {
        try {
            config.save(getConfigFilePath());
            logger.info("Saved configuration to " + getConfigFilePath());
        } catch (ConfigurationException e) {
            logger.error("Error while saving user configuration.", e);
        }
    }

    // TODO: Remove this and use SystemUtils instead.
    private static boolean isWindows() {
        return OS.contains("win");
    }

    // TODO: Remove this and use SystemUtils instead.
    private static boolean isMacOS() {
        return OS.contains("mac");
    }

    // TODO: Remove this and use SystemUtils instead.
    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("bsd");
    }

    private static String getWindowsConfigDir() {
        return System.getenv("LOCALAPPDATA") + File.separator + "ripme";
    }

    private static String getUnixConfigDir() {
        return System.getProperty("user.home") + File.separator + ".config" + File.separator + "ripme";
    }

    private static String getMacOSConfigDir() {
        return System.getProperty("user.home")
                + File.separator + "Library" + File.separator + "Application Support" + File.separator + "ripme";
    }

    private static boolean portableMode() {
        try {
            File f = new File(new File(".").getCanonicalPath() + File.separator + configFile);
            if(f.exists() && !f.isDirectory()) {
                logger.debug("Running in portable mode.");
                return true;
            }
        } catch (IOException e) {
            logger.debug("Running in non-portable mode.");
            return false;
        }
        return false;
    }


    public static String getConfigDir() {
        if (portableMode()) {
            try {
                return new File(".").getCanonicalPath();
            } catch (IOException e) {
                logger.error("Failed to get portable configuration file.", e);
            }
        }

        if (SystemUtils.IS_OS_WINDOWS) return getWindowsConfigDir();
        if (SystemUtils.IS_OS_MAC_OSX) return getMacOSConfigDir();
        // Testing for Linux, as IS_OS_UNIX would return true for Mac OS as well.
        if (SystemUtils.IS_OS_LINUX) return getUnixConfigDir();
        
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            logger.error("Failed to get current path.", e);
        }
    }

    /**
     * Deletes the url history file.
     */
    public static void clearURLHistory() {
        File file = new File(getURLHistoryFile());
        file.delete();
    }

    /**
     * Return the path of the url history file
      */
    public static String getURLHistoryFile() {
        return getConfigDir() + File.separator + "url_history.txt";
    }

    /**
     * Return the path of the configuration file
     */
    private static String getConfigFilePath() {
        return getConfigDir() + File.separator + configFile;
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
        } catch (IOException e) {
            logger.error("Error while removing current working directory from path.", e);
        }
        return prettySaveAs;
    }

    public static String stripURLParameter(String url, String parameter) {
        int paramIndex = url.indexOf("?" + parameter);
        boolean wasFirstParam = true;
        if (paramIndex < 0) {
            wasFirstParam = false;
            paramIndex = url.indexOf("&" + parameter);
        }

        if (paramIndex > 0) {
            int nextParam = url.indexOf("&", paramIndex+1);
            if (nextParam != -1) {
                String c = "&";
                if (wasFirstParam) {
                    c = "?";
                }
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
        ArrayList<Class<?>> classes = new ArrayList<>();
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
                while (entries.hasMoreElements()) {
                    JarEntry nextElement = entries.nextElement();
                    String entryName = nextElement.getName();
                    if (entryName.startsWith(relPath)
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

    private static final int SHORTENED_PATH_LENGTH = 12;
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

    public static String filesystemSanitized(String text) {
        text = text.replaceAll("[^a-zA-Z0-9.-]", "_");
        return text;
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

    /**
     * Checks if given path already exists as lowercase
     *
     * @param path - original path entered to be ripped
     * @return path of existing folder or the original path if not present
     */
    public static String getOriginalDirectory(String path) {

        int index;
        if(isUnix() || isMacOS()) {
            index = path.lastIndexOf('/');
        } else {
            // current OS is windows - nothing to do here
            return path;
        }

        String original = path;                                         // needs to be checked if lowercase exists
        String lastPart = original.substring(index+1).toLowerCase();    // setting lowercase to check if it exists

        // Get a List of all Directories and check its lowercase
        // if file exists return it
        File f = new File(path.substring(0, index));
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));

        for (String s : names) {
            if(s.toLowerCase().equals(lastPart)) {
                // Building Path of existing file
                return path.substring(0, index) + File.separator + s;
            }
        }

        return original;
    }

    public static String bytesToHumanReadable(int bytes) {
        float fbytes = (float) bytes;
        String[] mags = new String[] {"", "K", "M", "G", "T"};
        int magIndex = 0;
        while (fbytes >= 1024) {
            fbytes /= 1024;
            magIndex++;
        }
        return String.format("%.2f%siB", fbytes, mags[magIndex]);
    }

    public static List<String> getListOfAlbumRippers() {
        List<String> list = new ArrayList<>();
        for (Constructor<?> ripper : AbstractRipper.getRipperConstructors("com.rarchives.ripme.ripper.rippers")) {
            list.add(ripper.getName());
        }
        return list;
    }
    public static List<String> getListOfVideoRippers() {
        List<String> list = new ArrayList<>();
        for (Constructor<?> ripper : AbstractRipper.getRipperConstructors("com.rarchives.ripme.ripper.rippers.video")) {
            list.add(ripper.getName());
        }
        return list;
    }

    public static void playSound(String filename) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(filename);
        final Clip clip;
        try {
            clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.open(AudioSystem.getAudioInputStream(resource));
            clip.start();
        } catch (LineUnavailableException e) {
            logger.error("Line unavailable.", e);
        } catch (IOException e) {
            logger.error("Error opening audio file.", e);
        } catch (UnsupportedAudioFileException e) {
            logger.error("Unsupported audio file format.", e);
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
        } catch (IOException e) {
            logger.error("Error while closing resource file.", e);
        }
    }

    /**
     * Gets list of strings between two strings.
     * @param fullText Text to retrieve from.
     * @param start String that precedes the desired text
     * @param finish String that follows the desired text
     * @return List of all strings that are between 'start' and 'finish'
     */
    public static List<String> between(String fullText, String start, String finish) {
        List<String> result = new ArrayList<>();
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

    /**
     * Parses an URL query
     *
     * @param query
     *          The query part of an URL
     * @return The map of all query parameters
     */
    public static Map<String,String> parseUrlQuery(String query) {
        Map<String,String> res = new HashMap<>();

        if (query.equals("")) {
            return res;
        }

        String[] parts = query.split("&");
        int pos;

        try {
            for (String part : parts) {
                if ((pos = part.indexOf('=')) >= 0) {
                    res.put(URLDecoder.decode(part.substring(0, pos), "UTF-8"), URLDecoder.decode(part.substring(pos + 1), "UTF-8"));
                } else {
                    res.put(URLDecoder.decode(part, "UTF-8"), "");
                }
            }
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen since UTF-8 is required to be supported
            throw new RuntimeException(e);
        }

        return res;
    }

    /**
     * Parses an URL query and returns the requested parameter's value
     *
     * @param query
     *          The query part of an URL
     * @param key
     *          The key whose value is requested
     * @return The associated value or null if key wasn't found
     */
    public static String parseUrlQuery(String query, String key) {
        if (query.equals("")) {
            return null;
        }

        String[] parts = query.split("&");
        int pos;

        try {
            for (String part : parts) {
                if ((pos = part.indexOf('=')) >= 0) {
                    if (URLDecoder.decode(part.substring(0, pos), "UTF-8").equals(key)) {
                        return URLDecoder.decode(part.substring(pos + 1), "UTF-8");
                    }

                } else if (URLDecoder.decode(part, "UTF-8").equals(key)) {
                    return "";
                }
            }
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen since UTF-8 is required to be supported
            throw new RuntimeException(e);
        }

        return null;
    }

    private static HashMap<String, HashMap<String, String>> cookieCache;
    static {
        cookieCache = new HashMap<String, HashMap<String, String>>();
    }

    public static Map<String, String> getCookies(String host) {
        HashMap<String, String> domainCookies = cookieCache.get(host);
        if (domainCookies == null) {
            domainCookies = new HashMap<String, String>();
            String cookiesConfig = getConfigString("cookies." + host, "");
            for (String pair : cookiesConfig.split(" ")) {
                pair = pair.trim();
                if (pair.contains("=")) {
                    String[] pieces = pair.split("=", 2);
                    domainCookies.put(pieces[0], pieces[1]);
                }
            }
            cookieCache.put(host, domainCookies);
        }
        return domainCookies;
    }
}
