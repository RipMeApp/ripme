package com.rarchives.ripme.utils;

import com.rarchives.ripme.ripper.AbstractRipper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common utility functions used in various places throughout the project.
 */
public class Utils {

    private static final Pattern pattern = Pattern.compile("LabelsBundle_(?<lang>[A-Za-z_]+).properties");
    private static final String DEFAULT_LANG = "en_US";
    private static final String RIP_DIRECTORY = "rips";
    private static final String CONFIG_FILE = "rip.properties";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final Logger LOGGER = LogManager.getLogger(Utils.class);
    private static final int SHORTENED_PATH_LENGTH = 12;

    private static PropertiesConfiguration config;
    private static final HashMap<String, HashMap<String, String>> cookieCache;
    private static final HashMap<ByteBuffer, String> magicHash = new HashMap<>();

    private static ResourceBundle resourceBundle;

    static {
        cookieCache = new HashMap<>();

        try {
            String configPath = getConfigFilePath();
            Path file = Paths.get(configPath);

            if (!Files.exists(file)) {
                // Use default bundled with .jar
                configPath = CONFIG_FILE;
            }

            config = new PropertiesConfiguration(configPath);
            LOGGER.info("Loaded " + config.getPath());

            if (Files.exists(file)) {
                // Config was loaded from file
                if (!config.containsKey("twitter.auth") || !config.containsKey("twitter.max_requests")
                        || !config.containsKey("tumblr.auth") || !config.containsKey("error.skip404")
                        || !config.containsKey("gw.api") || !config.containsKey("page.timeout")
                        || !config.containsKey("download.max_size")) {
                    // Config is missing key fields
                    // Need to reload the default config
                    // See https://github.com/4pr0n/ripme/issues/158
                    LOGGER.warn("Config does not contain key fields, deleting old config");
                    Files.delete(file);
                    config = new PropertiesConfiguration(CONFIG_FILE);
                    LOGGER.info("Loaded " + config.getPath());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[!] Failed to load properties file from " + CONFIG_FILE, e);
        }

        resourceBundle = getResourceBundle(null);
    }

    /**
     * Get the root rips directory.
     *
     * @return Root directory to save rips to.
     */
    public static Path getWorkingDirectory() {
        String currentDir = getJarDirectory() + File.separator + RIP_DIRECTORY + File.separator;

        if (config != null) {
            currentDir = getConfigString("rips.directory", currentDir);
        }

        Path workingDir = Paths.get(currentDir);
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                LOGGER.error("WorkingDir " + workingDir + " not exists, and could not be created. Set to user.home, continue.");
                workingDir = Paths.get(System.getProperty("user.home"));
            }
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
    public static boolean isWindows() {
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
        return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support"
                + File.separator + "ripme";
    }

    private static Path getJarDirectory() {
        Path jarDirectory = Objects.requireNonNull(Utils.class.getResource("/rip.properties")).toString().contains("jar:")
                ? Paths.get(System.getProperty("java.class.path")).getParent()
                : Paths.get(System.getProperty("user.dir"));

        if (jarDirectory == null)
            jarDirectory = Paths.get(".");

        return jarDirectory;
    }

    /**
     * Determines if the app is running in a portable mode. i.e. on a USB stick
     */
    private static boolean portableMode() {
        Path file = getJarDirectory().resolve(CONFIG_FILE);
        return Files.exists(file) && !Files.isDirectory(file);
    }

    /**
     * Gets the directory of the config directory, for all systems.
     */
    public static String getConfigDir() {
        if (portableMode()) {
            try {
                return getJarDirectory().toAbsolutePath().toString();
            } catch (Exception e) {
                return ".";
            }
        }

        if (isWindows())
            return getWindowsConfigDir();
        if (isMacOS())
            return getMacOSConfigDir();
        if (isUnix())
            return getUnixConfigDir();

        try {
            return getJarDirectory().toAbsolutePath().toString();
        } catch (Exception e) {
            return ".";
        }
    }

    /**
     * Delete the url history file
     */
    public static void clearURLHistory() {
        Path file = Paths.get(getURLHistoryFile());
        try {
            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the path of the url history file
     */
    public static String getURLHistoryFile() {
        if (getConfigString("history.location", "").length() == 0) {
            return getConfigDir() + File.separator + "url_history.txt";
        } else {
            return getConfigString("history.location", "");
        }
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
    public static String removeCWD(Path saveAs) {
        try {
            return saveAs.relativize(Paths.get(".").toAbsolutePath()).toString();
        }
        catch (IllegalArgumentException e) {
            return saveAs.toString();
        }
    }

    /**
     * Strips away URL parameters, which usually appear at the end of URLs. E.g. the
     * ?query on PHP
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
                url = url.substring(0, paramIndex) + c + url.substring(nextParam + 1);
            } else {
                url = url.substring(0, paramIndex);
            }
        }

        return url;
    }

    /**
     * Get a list of all Classes within a package. Works with file system projects
     * and jar files! Borrowed from StackOverflow, but I don't have a link :[
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
            throw new RuntimeException(
                    pkgname + " (" + resource
                            + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
                    e);
        } catch (IllegalArgumentException e) {
            directory = null;
        }

        if (directory != null && directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            assert files != null;
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
                String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
                jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
                JarFile jarFile = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry nextElement = entries.nextElement();
                    String entryName = nextElement.getName();
                    if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())
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
        return shortenPath(path);
    }

    /**
     * Shortens the path to a file
     *
     * @param path File object that you want the shortened path of.
     * @return The simplified path to the file.
     */
    public static String shortenPath(Path path) {
        Path prettyPath = path.normalize();
        if (prettyPath.toString().length() < SHORTENED_PATH_LENGTH * 2) {
            return prettyPath.toString();
        }
        return prettyPath.toString().substring(0, SHORTENED_PATH_LENGTH)
                + "..."
                + prettyPath.toString().substring(prettyPath.toString().length() - SHORTENED_PATH_LENGTH);
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

    /**
     * Removes any potentially unsafe characters from a string and truncates it on a maximum length of 100 characters.
     * Characters considered safe are alpha numerical characters as well as minus, dot, comma, underscore and whitespace.
     *
     * @param text The potentially unsafe text
     * @return a filesystem safe string
     */
    public static String filesystemSafe(String text) {
        text = text.replaceAll("[^a-zA-Z0-9-.,_ ]", "").trim();
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
    public static String getOriginalDirectory(String path) throws IOException {

        int index;
        if (isUnix() || isMacOS()) {
            index = path.lastIndexOf('/');
        } else {
            // current OS is windows - nothing to do here
            return path;
        }

        String lastPart = path.substring(index + 1).toLowerCase(); // setting lowercase to check if it exists

        // Get a List of all Directories and check its lowercase
        // if file exists return it
        File file = new File(path.substring(0, index));
        if (!(file.isDirectory() && file.canWrite() && file.canExecute())) {
            throw new IOException("Original directory \"" + file + "\" is no directory or not writeable.");
        }
        ArrayList<String> names = new ArrayList<>(Arrays.asList(Objects.requireNonNull(file.list())));

        for (String name : names) {
            if (name.toLowerCase().equals(lastPart)) {
                // Building Path of existing file
                return path.substring(0, index) + File.separator + name;
            }
        }

        // otherwise return original path
        return path;
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
     * Gets and returns a list of all the album rippers present in the
     * "com.rarchives.ripme.ripper.rippers" package.
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
     * Gets and returns a list of all video rippers present in the
     * "com.rarchives.rime.rippers.video" package
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
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        // write to ripme.log file if checked in GUI
        boolean logSave = getConfigBoolean("log.save", false);
        if (logSave) {
            LOGGER.debug("add rolling appender ripmelog");
            TriggeringPolicy tp = SizeBasedTriggeringPolicy.createPolicy("20M");
            DefaultRolloverStrategy rs = DefaultRolloverStrategy.newBuilder().withMax("2").build();
            RollingFileAppender rolling = RollingFileAppender.newBuilder()
                    .setName("ripmelog")
                    .withFileName("ripme.log")
                    .withFilePattern("%d{yyyy-MM-dd HH:mm:ss} %p %m%n")
                    .withPolicy(tp)
                    .withStrategy(rs)
                    .build();
            loggerConfig.addAppender(rolling, null, null);
        } else {
            LOGGER.debug("remove rolling appender ripmelog");
            if (config.getAppender("ripmelog") != null) {
                config.getAppender("ripmelog").stop();
            }
            loggerConfig.removeAppender("ripmelog");
        }
        ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
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

        for (String part : parts) {
            if ((pos = part.indexOf('=')) >= 0) {
                res.put(URLDecoder.decode(part.substring(0, pos), StandardCharsets.UTF_8),
                        URLDecoder.decode(part.substring(pos + 1), StandardCharsets.UTF_8));
            } else {
                res.put(URLDecoder.decode(part, StandardCharsets.UTF_8), "");
            }
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

        for (String part : parts) {
            if ((pos = part.indexOf('=')) >= 0) {
                if (URLDecoder.decode(part.substring(0, pos), StandardCharsets.UTF_8).equals(key)) {
                    return URLDecoder.decode(part.substring(pos + 1), StandardCharsets.UTF_8);
                }

            } else if (URLDecoder.decode(part, StandardCharsets.UTF_8).equals(key)) {
                return "";
            }
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
     * Gets the ResourceBundle AKA language package. Used for choosing the language
     * of the UI.
     *
     * @return Returns the default resource bundle using the language specified in
     * the config file.
     */
    public static ResourceBundle getResourceBundle(String langSelect) {
        if (langSelect == null) {
            if (!getConfigString("lang", "").equals("")) {
                String[] langCode = getConfigString("lang", "").split("_");
                LOGGER.info("Setting locale to " + getConfigString("lang", ""));
                return ResourceBundle.getBundle("LabelsBundle", new Locale(langCode[0], langCode[1]),
                        new UTF8Control());
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

    public static void setLanguage(String langSelect) {
        resourceBundle = getResourceBundle(langSelect);
    }

    public static String getSelectedLanguage() {
        return resourceBundle.getLocale().toString();
    }

    // All the langs ripme has been translated into
    public static String[] getSupportedLanguages() {
        ArrayList<Path> filesList = new ArrayList<>();
        try {
            URI uri = Objects.requireNonNull(Utils.class.getResource("/rip.properties")).toURI();

            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/");
            } else {
                myPath = Paths.get(uri).getParent();
            }

            Files.walk(myPath, 1).filter(p -> p.toString().contains("LabelsBundle_")).distinct()
                    .forEach(filesList::add);

            String[] langs = new String[filesList.size()];
            for (int i = 0; i < filesList.size(); i++) {
                Matcher matcher = pattern.matcher(filesList.get(i).toString());
                if (matcher.find())
                    langs[i] = matcher.group("lang");
            }

            return langs;
        } catch (Exception e) {
            e.printStackTrace();
            // On error return default language
            return new String[]{DEFAULT_LANG};
        }
    }

    public static String getLocalizedString(String key) {
        LOGGER.debug(String.format("Key %s in %s is: %s", key, getSelectedLanguage(),
                resourceBundle.getString(key)));
        return resourceBundle.getString(key);
    }

    /**
     * Formats and reuturns the status text for rippers using the byte progress bar
     *
     * @param completionPercentage An int between 0 and 100 which repersents how
     *                             close the download is to complete
     * @param bytesCompleted       How many bytes have been downloaded
     * @param bytesTotal           The total size of the file that is being
     *                             downloaded
     * @return Returns the formatted status text for rippers using the byte progresbar
     */
    public static String getByteStatusText(int completionPercentage, int bytesCompleted, int bytesTotal) {
        return completionPercentage + "%  - " + Utils.bytesToHumanReadable(bytesCompleted) + " / "
                + Utils.bytesToHumanReadable(bytesTotal);
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
    public static boolean fuzzyExists(Path folder, String filename) {
        return Files.exists(folder.resolve(filename));
    }

    public static Path getPath(String pathToSanitize) {
        return Paths.get(sanitizeSaveAs(pathToSanitize));
    }

    public static String sanitizeSaveAs(String fileNameToSan) {
        return fileNameToSan.replaceAll("[\\\\:*?\"<>|]", "_");
    }

    public static Path shortenSaveAsWindows(String ripsDirPath, String fileName) throws FileNotFoundException {
        LOGGER.error("The filename " + fileName + " is to long to be saved on this file system.");
        LOGGER.info("Shortening filename");
        String fullPath = ripsDirPath + File.separator + fileName;
        // How long the path without the file name is
        int pathLength = ripsDirPath.length();
        if (pathLength == 260) {
            // We've reached the max length, there's nothing more we can do
            throw new FileNotFoundException("File path is too long for this OS");
        }
        String[] saveAsSplit = fileName.split("\\.");
        // Get the file extension so when we shorten the file name we don't cut off the
        // file extension
        String fileExt = saveAsSplit[saveAsSplit.length - 1];
        // The max limit for paths on Windows is 260 chars
        fullPath = fullPath.substring(0, 259 - pathLength - fileExt.length() + 1) + "." + fileExt;
        LOGGER.info(fullPath);
        LOGGER.info(fullPath.length());
        return Paths.get(fullPath);
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (final InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
