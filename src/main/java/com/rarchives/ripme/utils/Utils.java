package com.rarchives.ripme.utils;

import com.rarchives.ripme.ripper.AbstractRipper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Common utility functions used in various places throughout the project.
 */
public class Utils {

    private static final String RIP_DIRECTORY = "rips";
    private static final String CONFIG_FILE = "rip.properties";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final Logger LOGGER = Logger.getLogger(Utils.class);
    private static final int SHORTENED_PATH_LENGTH = 12;

    private static PropertiesConfiguration config;
    private static HashMap<String, HashMap<String, String>> cookieCache;
    private static HashMap<ByteBuffer, String> magicHash = new HashMap<>();

    static {
        cookieCache = new HashMap<>();

        try {
            String configPath = getConfigFilePath();
            File file = new File(configPath);

            if (!file.exists()) {
                // Use default bundled with .jar
                configPath = CONFIG_FILE;
            }

            config = new PropertiesConfiguration(configPath);
            LOGGER.info("Loaded " + config.getPath());

            if (file.exists()) {
                // Config was loaded from file
                if (!config.containsKey("twitter.auth") || !config.containsKey("twitter.max_requests")
                        || !config.containsKey("tumblr.auth") || !config.containsKey("error.skip404")
                        || !config.containsKey("gw.api") || !config.containsKey("page.timeout")
                        || !config.containsKey("download.max_size")) {
                    // Config is missing key fields
                    // Need to reload the default config
                    // See https://github.com/4pr0n/ripme/issues/158
                    LOGGER.warn("Config does not contain key fields, deleting old config");
                    file.delete();
                    config = new PropertiesConfiguration(CONFIG_FILE);
                    LOGGER.info("Loaded " + config.getPath());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[!] Failed to load properties file from " + CONFIG_FILE, e);
        }
    }

    /**
     * Get the root rips directory.
     *
     * @return Root directory to save rips to.
     */
    public static File getWorkingDirectory() {
        String currentDir = ".";
        try {
            currentDir = new File(".").getCanonicalPath() + File.separator + RIP_DIRECTORY + File.separator;
        } catch (IOException e) {
            LOGGER.error("Error while finding working dir: ", e);
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

    /**
     * Gets the value of a specific config key.
     *
     * @param key          The name of the config parameter you want to find.
     * @param defaultValue What the default value would be.
     */
    public static String getConfigString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static String[] getConfigStringArray(String key) {
        String[] configStringArray = config.getStringArray(key);

        return configStringArray.length == 0 ? null : configStringArray;
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
                result.add((String) obj);
            }
        }
        return result;
    }

    public static void setConfigBoolean(String key, boolean value) {
        config.setProperty(key, value);
    }

    public static void setConfigString(String key, String value) {
        config.setProperty(key, value);
    }

    public static void setConfigInteger(String key, int value) {
        config.setProperty(key, value);
    }

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
            LOGGER.info("Saved configuration to " + getConfigFilePath());
        } catch (ConfigurationException e) {
            LOGGER.error("Error while saving configuration: ", e);
        }
    }

    /**
     * Determines if your current system is a Windows system.
     */
    private static boolean isWindows() {
        return OS.contains("win");
    }

    /**
     * Determines if your current system is a Mac system
     */
    private static boolean isMacOS() {
        return OS.contains("mac");
    }

    /**
     * Determines if current system is based on UNIX
     */
    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("bsd");
    }

    /**
     * Gets the directory of where the config file is stored on a Windows machine.
     */
    private static String getWindowsConfigDir() {
        return System.getenv("LOCALAPPDATA") + File.separator + "ripme";
    }

    /**
     * Gets the directory of where the config file is stored on a UNIX machine.
     */
    private static String getUnixConfigDir() {
        return System.getProperty("user.home") + File.separator + ".config" + File.separator + "ripme";
    }

    /**
     * Gets the directory of where the config file is stored on a Mac machine.
     */
    private static String getMacOSConfigDir() {
        return System.getProperty("user.home")
                + File.separator + "Library" + File.separator + "Application Support" + File.separator + "ripme";
    }

    /**
     * Determines if the app is running in a portable mode. i.e. on a USB stick
     */
    private static boolean portableMode() {
        try {
            File file = new File(new File(".").getCanonicalPath() + File.separator + CONFIG_FILE);
            if (file.exists() && !file.isDirectory()) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    /**
     * Gets the directory of the config directory, for all systems.
     */
    public static String getConfigDir() {
        if (portableMode()) {
            try {
                return new File(".").getCanonicalPath();
            } catch (Exception e) {
                return ".";
            }
        }

        if (isWindows()) return getWindowsConfigDir();
        if (isMacOS()) return getMacOSConfigDir();
        if (isUnix()) return getUnixConfigDir();

        try {
            return new File(".").getCanonicalPath();
        } catch (Exception e) {
            return ".";
        }
    }

    /**
     * Delete the url history file
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
     * Gets the path to the configuration file.
     */
    private static String getConfigFilePath() {
        return getConfigDir() + File.separator + CONFIG_FILE;
    }

    /**
     * Removes the current working directory (CWD) from a File.
     *
     * @param saveAs The File path
     * @return saveAs in relation to the CWD
     */
    public static String removeCWD(File saveAs) {
        String prettySaveAs = saveAs.toString();
        try {
            prettySaveAs = saveAs.getCanonicalPath();
            String cwd = new File(".").getCanonicalPath() + File.separator;
            prettySaveAs = prettySaveAs.replace(cwd, "." + File.separator);
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
        return prettySaveAs;
    }

    /**
     * Strips away URL parameters, which usually appear at the end of URLs.
     * E.g. the ?query on PHP
     *
     * @param url       The URL to filter/strip
     * @param parameter The parameter to strip
     * @return The stripped URL
     */
    public static String stripURLParameter(String url, String parameter) {
        int paramIndex = url.indexOf("?" + parameter);
        boolean wasFirstParam = true;
        if (paramIndex < 0) {
            wasFirstParam = false;
            paramIndex = url.indexOf("&" + parameter);
        }

        if (paramIndex > 0) {
            int nextParam = url.indexOf('&', paramIndex + 1);
            if (nextParam != -1) {
                String c = "&";
                if (wasFirstParam) {
                    c = "?";
                }
                url = url.substring(0, paramIndex) + c + url.substring(nextParam + 1, url.length());
            } else {
                url = url.substring(0, paramIndex);
            }
        }

        return url;
    }

    /**
     * Removes the current working directory from a given filename
     *
     * @param file Path to the file
     * @return 'file' without the leading current working directory
     */
    public static String removeCWD(String file) {
        return removeCWD(new File(file));
    }

    /**
     * Get a list of all Classes within a package.
     * Works with file system projects and jar files!
     * Borrowed from StackOverflow, but I don't have a link :[
     *
     * @param pkgname The name of the package
     * @return List of classes within the package
     */
    public static List<Class<?>> getClassesForPackage(String pkgname) {
        ArrayList<Class<?>> classes = new ArrayList<>();
        String relPath = pkgname.replace('.', '/');
        URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
        if (resource == null) {
            throw new RuntimeException("No resource for " + relPath);
        }

        String fullPath = resource.getFile();
        File directory;

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
        } else {
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
                            LOGGER.error("ClassNotFoundException loading " + className);
                            jarFile.close(); // Resource leak fix?
                            throw new RuntimeException("ClassNotFoundException loading " + className);
                        }
                    }
                }
                jarFile.close(); // Eclipse said not closing it would have a resource leak
            } catch (IOException e) {
                LOGGER.error("Error while loading jar file:", e);
                throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
            }
        }
        return classes;
    }

    /**
     * Shortens the path to a file
     *
     * @param path String of the path to the file
     * @return The simplified path to the file.
     */
    public static String shortenPath(String path) {
        return shortenPath(new File(path));
    }

    /**
     * Shortens the path to a file
     *
     * @param file File object that you want the shortened path of.
     * @return The simplified path to the file.
     */
    public static String shortenPath(File file) {
        String path = removeCWD(file);
        if (path.length() < SHORTENED_PATH_LENGTH * 2) {
            return path;
        }
        return path.substring(0, SHORTENED_PATH_LENGTH)
                + "..."
                + path.substring(path.length() - SHORTENED_PATH_LENGTH);
    }

    /**
     * Sanitizes a string so that a filesystem can handle it
     *
     * @param text The text to be sanitized.
     * @return The sanitized text.
     */
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
        if (isUnix() || isMacOS()) {
            index = path.lastIndexOf('/');
        } else {
            // current OS is windows - nothing to do here
            return path;
        }

        String original = path;                                         // needs to be checked if lowercase exists
        String lastPart = original.substring(index + 1).toLowerCase();    // setting lowercase to check if it exists

        // Get a List of all Directories and check its lowercase
        // if file exists return it
        File file = new File(path.substring(0, index));
        ArrayList<String> names = new ArrayList<>(Arrays.asList(file.list()));

        for (String name : names) {
            if (name.toLowerCase().equals(lastPart)) {
                // Building Path of existing file
                return path.substring(0, index) + File.separator + name;
            }
        }

        return original;
    }

    /**
     * Converts an integer into a human readable string
     *
     * @param bytes Non-human readable integer.
     * @return Human readable interpretation of a byte.
     */
    public static String bytesToHumanReadable(int bytes) {
        float fbytes = (float) bytes;
        String[] mags = new String[]{"", "K", "M", "G", "T"};
        int magIndex = 0;
        while (fbytes >= 1024) {
            fbytes /= 1024;
            magIndex++;
        }
        return String.format("%.2f%siB", fbytes, mags[magIndex]);
    }

    /**
     * Gets and returns a list of all the album rippers present in the "com.rarchives.ripme.ripper.rippers" package.
     *
     * @return List<String> of all album rippers present.
     */
    public static List<String> getListOfAlbumRippers() throws Exception {
        List<String> list = new ArrayList<>();
        for (Constructor<?> ripper : AbstractRipper.getRipperConstructors("com.rarchives.ripme.ripper.rippers")) {
            list.add(ripper.getName());
        }
        return list;
    }

    /**
     * Gets and returns a list of all video rippers present in the "com.rarchives.rime.rippers.video" package
     *
     * @return List<String> of all the video rippers.
     */
    public static List<String> getListOfVideoRippers() throws Exception {
        List<String> list = new ArrayList<>();
        for (Constructor<?> ripper : AbstractRipper.getRipperConstructors("com.rarchives.ripme.ripper.rippers.video")) {
            list.add(ripper.getName());
        }
        return list;
    }

    /**
     * Plays a sound from a file.
     *
     * @param filename Path to the sound file
     */
    public static void playSound(String filename) {
        URL resource = ClassLoader.getSystemClassLoader().getResource(filename);
        try {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.open(AudioSystem.getAudioInputStream(resource));
            clip.start();
        } catch (Exception e) {
            LOGGER.error("Failed to play sound " + filename, e);
        }
    }

    /**
     * Configures root logger, either for FILE output or just console.
     */
    public static void configureLogger() {
        LogManager.shutdown();
        String logFile = getConfigBoolean("log.save", false) ? "log4j.file.properties" : "log4j.properties";

        try (InputStream stream = Utils.class.getClassLoader().getResourceAsStream(logFile)) {
            if (stream == null) {
                PropertyConfigurator.configure("src/main/resources/" + logFile);
            } else {
                PropertyConfigurator.configure(stream);
            }

            LOGGER.info("Loaded " + logFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    /**
     * Gets list of strings between two strings.
     *
     * @param fullText Text to retrieve from.
     * @param start    String that precedes the desired text
     * @param finish   String that follows the desired text
     * @return List of all strings that are between 'start' and 'finish'
     */
    public static List<String> between(String fullText, String start, String finish) {
        List<String> result = new ArrayList<>();
        int i = fullText.indexOf(start);

        while (i >= 0) {
            i += start.length();
            int j = fullText.indexOf(finish, i);
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
     * @param query The query part of an URL
     * @return The map of all query parameters
     */
    public static Map<String, String> parseUrlQuery(String query) {
        Map<String, String> res = new HashMap<>();

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
     * @param query The query part of an URL
     * @param key   The key whose value is requested
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

    /**
     * Gets all the cookies from a certain host
     */
    public static Map<String, String> getCookies(String host) {
        HashMap<String, String> domainCookies = cookieCache.get(host);
        if (domainCookies == null) {
            domainCookies = new HashMap<>();
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

    /**
     * Gets the ResourceBundle AKA language package.
     * Used for choosing the language of the UI.
     *
     * @return Returns the default resource bundle using the language specified in the config file.
     */
    public static ResourceBundle getResourceBundle(String langSelect) {
        if (langSelect == null) {
            if (!getConfigString("lang", "").equals("")) {
                String[] langCode = getConfigString("lang", "").split("_");
                LOGGER.info("Setting locale to " + getConfigString("lang", ""));
                return ResourceBundle.getBundle("LabelsBundle", new Locale(langCode[0], langCode[1]), new UTF8Control());
            }
        } else {
            String[] langCode = langSelect.split("_");
            LOGGER.info("Setting locale to " + langSelect);
            return ResourceBundle.getBundle("LabelsBundle", new Locale(langCode[0], langCode[1]), new UTF8Control());
        }
        try {
            LOGGER.info("Setting locale to default");
            return ResourceBundle.getBundle("LabelsBundle", Locale.getDefault(), new UTF8Control());
        } catch (MissingResourceException e) {
            LOGGER.info("Setting locale to root");
            return ResourceBundle.getBundle("LabelsBundle", Locale.ROOT);
        }
    }

    /**
     * Formats and reuturns the status text for rippers using the byte progress bar
     *
     * @param completionPercentage An int between 0 and 100 which repersents how close the download is to complete
     * @param bytesCompleted How many bytes have been downloaded
     * @param bytesTotal The total size of the file that is being downloaded
     * @return Returns the formatted status text for rippers using the byte progress bar
     */
    public static String getByteStatusText(int completionPercentage, int bytesCompleted, int bytesTotal) {
        return String.valueOf(completionPercentage) +
                "%  - " +
                Utils.bytesToHumanReadable(bytesCompleted) +
                " / " +
                Utils.bytesToHumanReadable(bytesTotal);
    }

    public static String getEXTFromMagic(ByteBuffer magic) {
        if (magicHash.isEmpty()) {
            LOGGER.debug("initialising map");
            initialiseMagicHashMap();
        }
        return magicHash.get(magic);
    }

    public static String getEXTFromMagic(byte[] magic) {
        return getEXTFromMagic(ByteBuffer.wrap(magic));
    }

    private static void initialiseMagicHashMap() {
        magicHash.put(ByteBuffer.wrap(new byte[]{-1, -40, -1, -37, 0, 0, 0, 0}), "jpeg");
        magicHash.put(ByteBuffer.wrap(new byte[]{-119, 80, 78, 71, 13, 0, 0, 0}), "png");
    }

    // Checks if a file exists ignoring it's extension.
    // Code from: https://stackoverflow.com/a/17698068
    public static boolean fuzzyExists(File folder, String fileName) {
        if (!folder.exists()) {
            return false;
        }
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return false;
        }

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String[] filename = file.getName().split("\\.(?=[^\\.]+$)"); //split filename from it's extension
                if(filename[0].equalsIgnoreCase(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

}