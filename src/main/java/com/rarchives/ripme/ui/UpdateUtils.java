package com.rarchives.ripme.ui;

import com.rarchives.ripme.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class UpdateUtils {

    private static final Logger logger = LogManager.getLogger(UpdateUtils.class);
    // do not update the default version without adjusting the unit test. the real version comes from METAINF.MF
    private static final String DEFAULT_VERSION = "1.7.94-10-b6345398";
    private static final String REPO_NAME = "laziassdev/ripme";
    private static final String updateJsonURL = "https://raw.githubusercontent.com/" + REPO_NAME + "/main/ripme.json";
    private static final Path newFile = Paths.get("ripme.jar.new");
    private static Path mainFile;
    private static JSONObject ripmeJson;

    static {
        try {
            mainFile = Paths.get(UpdateUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException | IllegalArgumentException e) {
            mainFile = Paths.get("ripme.jar");
            logger.error("Unable to get path of jar");
            e.printStackTrace();
        }
    }

    private static String getUpdateJarURL(String latestVersion) {
        // this works with a tag created in github, and thus download URLs like:
        // https://github.com/ripmeapp2/ripme/releases/download/2.0.4/ripme-2.0.4-12-487e38cc.jar
        return "https://github.com/"
                + REPO_NAME
                + "/releases/download/"
                + latestVersion
                + "/ripme-"
                + latestVersion + ".jar";
    }

    public static String getThisJarVersion() {
        String thisVersion = UpdateUtils.class.getPackage().getImplementationVersion();
        if (thisVersion == null) {
            // Version is null if we're not running from the JAR
            thisVersion = DEFAULT_VERSION; // Super-high version number
        }
        return thisVersion;
    }

    private static String getChangeList(JSONObject rj) {
        JSONArray jsonChangeList = rj.getJSONArray("changeList");
        StringBuilder changeList = new StringBuilder();
        for (int i = 0; i < jsonChangeList.length(); i++) {
            String change = jsonChangeList.getString(i);
            if (change.startsWith(UpdateUtils.getThisJarVersion() + ":")) {
                break;
            }
            changeList.append("\n").append(change);
        }
        return changeList.toString();
    }

    public static void updateProgramCLI() {
        logger.info("Checking for update...");

        Document doc;
        try {
            logger.debug("Retrieving " + UpdateUtils.updateJsonURL);
            doc = Jsoup.connect(UpdateUtils.updateJsonURL).timeout(10 * 1000).ignoreContentType(true).get();
        } catch (IOException e) {
            logger.error("Error while fetching update: ", e);
            JOptionPane.showMessageDialog(null,
                    "<html><font color=\"red\">Error while fetching update: " + e.getMessage() + "</font></html>",
                    "RipMe Updater", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            logger.info("Current version: " + getThisJarVersion());
        }
        String jsonString = doc.body().html().replaceAll("&quot;", "\"");
        ripmeJson = new JSONObject(jsonString);

        String changeList = getChangeList(ripmeJson);

        logger.info("Change log: \n" + changeList);

        String latestVersion = ripmeJson.getString("latestVersion");
        if (UpdateUtils.isNewerVersion(latestVersion)) {
            logger.info("Found newer version: " + latestVersion);
            logger.info("Downloading" +getUpdateJarURL(latestVersion) + " ...");
            try {
                UpdateUtils.downloadJarAndLaunch(getUpdateJarURL(latestVersion), false);
            } catch (IOException e) {
                logger.error("Error while updating: ", e);
            }
        } else {
            logger.info("Running version (" + UpdateUtils.getThisJarVersion()
                    + ") is not older than release (" + latestVersion + ")");
        }
    }

    public static void updateProgramGUI(JLabel configUpdateLabel) {
        configUpdateLabel.setText("Checking for update...");

        Document doc;
        try {
            logger.debug("Retrieving " + UpdateUtils.updateJsonURL);
            doc = Jsoup.connect(UpdateUtils.updateJsonURL).timeout(10 * 1000).ignoreContentType(true).get();
        } catch (IOException e) {
            logger.error("Error while fetching update: ", e);
            JOptionPane.showMessageDialog(null,
                    "<html><font color=\"red\">Error while fetching update: " + e.getMessage() + "</font></html>",
                    "RipMe Updater", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            configUpdateLabel.setText("Current version: " + getThisJarVersion());
        }
        String jsonString = doc.body().html().replaceAll("&quot;", "\"");
        ripmeJson = new JSONObject(jsonString);

        String changeList = getChangeList(ripmeJson);

        String latestVersion = ripmeJson.getString("latestVersion");
        if (UpdateUtils.isNewerVersion(latestVersion)) {
            logger.info("Found newer version: " + latestVersion);
            JEditorPane changeListPane = new JEditorPane("text/html", String.format(
                    "<html><font color=\"green\">New version (%s) is available!</font>" + "<br><br>Recent changes: %s"
                            + "<br><br>Do you want to download and run the newest version?</html>",
                    latestVersion, changeList.replaceAll("\\n", "<br><br>")));
            changeListPane.setEditable(false);
            JScrollPane changeListScrollPane = new JScrollPane(changeListPane);
            changeListScrollPane.setPreferredSize(new Dimension(300, 300));
            int result = JOptionPane.showConfirmDialog(null, changeListScrollPane, "RipMe Updater",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                configUpdateLabel.setText("<html>Current Version: " + getThisJarVersion()
                        + "<br><font color=\"green\">Latest version: " + latestVersion + "</font></html>");
                return;
            }
            configUpdateLabel.setText("<html><font color=\"green\">Downloading new version...</font></html>");
            logger.info("New version found, downloading " + getUpdateJarURL(latestVersion));
            try {
                UpdateUtils.downloadJarAndLaunch(getUpdateJarURL(latestVersion), true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error while updating: " + e.getMessage(), "RipMe Updater",
                        JOptionPane.ERROR_MESSAGE);
                configUpdateLabel.setText("");
                logger.error("Error while updating: ", e);
            }
        } else {
            logger.info("Running version (" + UpdateUtils.getThisJarVersion()
                    + ") is not older than release (" + latestVersion + ")");
            configUpdateLabel.setText("<html><font color=\"green\">v" + UpdateUtils.getThisJarVersion()
                    + " is the latest version</font></html>");
        }
    }

    static boolean isNewerVersion(String latestVersion) {
        // If we're testing the update utils we want the program to always try to update
        if (Utils.getConfigBoolean("testing.always_try_to_update", false)) {
            logger.info("isNewerVersion is returning true because the key \"testing.always_try_to_update\" is true");
            return true;
        }
        int[] oldVersions = versionStringToInt(getThisJarVersion());
        int[] newVersions = versionStringToInt(latestVersion);
        if (oldVersions.length < newVersions.length) {
            logger.error("Calculated: " + getThisJarVersion() + " < " + latestVersion);
            return true;
        }

        for (int i = 0; i < oldVersions.length; i++) {
            if (newVersions[i] > oldVersions[i]) {
                logger.debug("oldVersion " + getThisJarVersion() + " < latestVersion " + latestVersion);
                return true;
            } else if (newVersions[i] < oldVersions[i]) {
                logger.debug("oldVersion " + getThisJarVersion() + " > latestVersion " + latestVersion);
                return false;
            }
        }

        // At this point, the version numbers are exactly the same.
        // Assume any additional changes to the version text means a new version
        return !(latestVersion.equals(getThisJarVersion()));
    }

    private static int[] versionStringToInt(String version) {
        // a version string looks like 1.7.94, 1.7.94-10-something
        // 10 is the number of commits since the 1.7.94 tag, so newer
        // the int array returned then contains e.g. 1.7.94.0 or 1.7.94.10
        String[] strVersions = version.split("[.-]");
        // not consider more than 4 components of version, loop only the real number
        // of components or maximum 4 components of the version string
        int[] intVersions = new int[4];
        for (int i = 0; i < Math.min(4, strVersions.length); i++) {
            // if it is an integer, set it, otherwise leave default 0
            if (strVersions[i].matches("\\d+")) {
                intVersions[i] = Integer.parseInt(strVersions[i]);
            }
        }
        return intVersions;
    }

    // Code take from https://stackoverflow.com/a/30925550
    public static String createSha256(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream fis = Files.newInputStream(file)) {
                int n = 0;
                byte[] buffer = new byte[8192];
                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        digest.update(buffer, 0, n);
                    }
                }
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
                sb.append("0123456789ABCDEF".charAt((b & 0x0F)));
            }
            // As patch.py writes the hash in lowercase this must return the has in
            // lowercase
            return sb.toString().toLowerCase();
        } catch (FileNotFoundException e) {
            logger.error("Could not find file: " + file);
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Got error getting file hash " + e.getMessage());
        }
        return null;
    }

    private static void downloadJarAndLaunch(String updateJarURL, Boolean shouldLaunch) throws IOException {
        Response response;
        response = Jsoup.connect(updateJarURL).ignoreContentType(true)
                .timeout(Utils.getConfigInteger("download.timeout", 60 * 1000)).maxBodySize(1024 * 1024 * 100)
                .execute();

        try (OutputStream out = Files.newOutputStream(newFile)) {
            out.write(response.bodyAsBytes());
        }
        // Only check the hash if the user hasn't disabled hash checking
        if (Utils.getConfigBoolean("security.check_update_hash", true)) {
            String updateHash = createSha256(newFile);
            logger.info("Download of new version complete; saved to " + newFile);
            logger.info("Checking hash of update");

            if (!ripmeJson.getString("currentHash").equals(updateHash)) {
                logger.error("Error: Update has bad hash");
                logger.debug("Expected hash: " + ripmeJson.getString("currentHash"));
                logger.debug("Actual hash: " + updateHash);
                throw new IOException("Got bad file hash");
            } else {
                logger.info("Hash is good");
            }
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows
            final Path batchFile = Paths.get("update_ripme.bat");
            String script = "@echo off\r\n" + "timeout 1\r\n"
                    + "copy \"" + newFile + "\" \"" + mainFile + "\"\r\n"
                    + "del \"" + newFile + "\"\r\n";

            if (shouldLaunch)
                script += "\"" + mainFile + "\"\r\n";
            script += "del \"" + batchFile + "\"\r\n";

            // Create updater script
            try (BufferedWriter bw = Files.newBufferedWriter(batchFile)) {
                bw.write(script);
                bw.flush();
            }

            logger.info("Saved update script to " + batchFile);
            // Run updater script on exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Executing: " + batchFile);
                    ProcessBuilder processBuilder = new ProcessBuilder(String.valueOf(batchFile));
                    processBuilder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            logger.info("Exiting older version, should execute update script (" + batchFile + ") during exit");
            System.exit(0);
        } else {
            // Mac / Linux
            // Modifying file and launching it: *nix distributions don't have any issues
            // with modifying/deleting files
            // while they are being run
            Files.move(newFile, mainFile, REPLACE_EXISTING);
            if (shouldLaunch) {
                // No need to do it during shutdown: the file used will indeed be the new one
                logger.info("Executing: " + mainFile);
                Runtime.getRuntime().exec(new String[]{"java", "-jar", mainFile.toString()});
            }
            logger.info("Update installed, newer version should be executed upon relaunch");
            System.exit(0);
        }
    }
}