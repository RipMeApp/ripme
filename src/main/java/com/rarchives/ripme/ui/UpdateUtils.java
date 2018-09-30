package com.rarchives.ripme.ui;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.utils.Utils;

public class UpdateUtils {

    private static final Logger logger = Logger.getLogger(UpdateUtils.class);
    private static final String DEFAULT_VERSION = "1.7.66";
    private static final String REPO_NAME = "ripmeapp/ripme";
    private static final String updateJsonURL = "https://raw.githubusercontent.com/" + REPO_NAME + "/master/ripme.json";
    private static final String mainFileName = "ripme.jar";
    private static final String updateFileName = "ripme.jar.update";
    private static JSONObject ripmeJson;

    private static String getUpdateJarURL(String latestVersion) {
        return "https://github.com/" + REPO_NAME + "/releases/download/" + latestVersion + "/ripme.jar";
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

        Document doc = null;
        try {
            logger.debug("Retrieving " + UpdateUtils.updateJsonURL);
            doc = Jsoup.connect(UpdateUtils.updateJsonURL)
                    .timeout(10 * 1000)
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            logger.error("Error while fetching update: ", e);
            JOptionPane.showMessageDialog(null,
                    "<html><font color=\"red\">Error while fetching update: " + e.getMessage() + "</font></html>",
                    "RipMe Updater",
                    JOptionPane.ERROR_MESSAGE);
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
            logger.info("Downloading new version...");
            logger.info("New version found, downloading...");
            try {
                UpdateUtils.downloadJarAndLaunch(getUpdateJarURL(latestVersion), false);
            } catch (IOException e) {
                logger.error("Error while updating: ", e);
            }
        } else {
            logger.debug("This version (" + UpdateUtils.getThisJarVersion() +
                    ") is the same or newer than the website's version (" + latestVersion + ")");
            logger.info("v" + UpdateUtils.getThisJarVersion() + " is the latest version");
            logger.debug("Running latest version: " + UpdateUtils.getThisJarVersion());
        }
    }

    public static void updateProgramGUI(JLabel configUpdateLabel) {
        configUpdateLabel.setText("Checking for update...");

        Document doc = null;
        try {
            logger.debug("Retrieving " + UpdateUtils.updateJsonURL);
            doc = Jsoup.connect(UpdateUtils.updateJsonURL)
                       .timeout(10 * 1000)
                       .ignoreContentType(true)
                       .get();
        } catch (IOException e) {
            logger.error("Error while fetching update: ", e);
            JOptionPane.showMessageDialog(null,
                    "<html><font color=\"red\">Error while fetching update: " + e.getMessage() + "</font></html>",
                    "RipMe Updater",
                    JOptionPane.ERROR_MESSAGE);
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
            int result = JOptionPane.showConfirmDialog(
                    null,
                    String.format("<html><font color=\"green\">New version (%s) is available!</font>"
                            + "<br><br>Recent changes: %s"
                            + "<br><br>Do you want to download and run the newest version?</html>", latestVersion, changeList.replaceAll("\n", "")),
                    "RipMe Updater",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                configUpdateLabel.setText("<html>Current Version: " + getThisJarVersion()
                        + "<br><font color=\"green\">Latest version: " + latestVersion + "</font></html>");
                return;
            }
            configUpdateLabel.setText("<html><font color=\"green\">Downloading new version...</font></html>");
            logger.info("New version found, downloading...");
            try {
                UpdateUtils.downloadJarAndLaunch(getUpdateJarURL(latestVersion), true);
            } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error while updating: " + e.getMessage(),
                    "RipMe Updater",
                    JOptionPane.ERROR_MESSAGE);
            configUpdateLabel.setText("");
                logger.error("Error while updating: ", e);
            }
        } else {
            logger.debug("This version (" + UpdateUtils.getThisJarVersion() +
                        ") is the same or newer than the website's version (" + latestVersion + ")");
            configUpdateLabel.setText("<html><font color=\"green\">v" + UpdateUtils.getThisJarVersion() + " is the latest version</font></html>");
            logger.debug("Running latest version: " + UpdateUtils.getThisJarVersion());
        }
    }

    private static boolean isNewerVersion(String latestVersion) {
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
                logger.debug("oldVersion " + getThisJarVersion() + " < latestVersion" + latestVersion);
                return true;
            }
            else if (newVersions[i] < oldVersions[i]) {
                logger.debug("oldVersion " + getThisJarVersion() + " > latestVersion " + latestVersion);
                return false;
            }
        }

        // At this point, the version numbers are exactly the same.
        // Assume any additional changes to the version text means a new version
        return !(latestVersion.equals(getThisJarVersion()));
    }

    private static int[] versionStringToInt(String version) {
        String strippedVersion = version.split("-")[0];
        String[] strVersions = strippedVersion.split("\\.");
        int[] intVersions = new int[strVersions.length];
        for (int i = 0; i < strVersions.length; i++) {
            intVersions[i] = Integer.parseInt(strVersions[i]);
        }
        return intVersions;
    }

    // Code take from https://stackoverflow.com/a/30925550
    private static String createSha256(File file)  {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream fis = new FileInputStream(file);
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            // As patch.py writes the hash in lowercase this must return the has in lowercase
            return new HexBinaryAdapter().marshal(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Got error getting file hash " + e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("Could not find file: " + file.getName());
        } catch (IOException e) {
            logger.error("Got error getting file hash " + e.getMessage());
        }
        return null;
    }

    private static void downloadJarAndLaunch(String updateJarURL, Boolean shouldLaunch)
            throws IOException {
        Response response;
        response = Jsoup.connect(updateJarURL)
                .ignoreContentType(true)
                .timeout(Utils.getConfigInteger("download.timeout", 60 * 1000))
                .maxBodySize(1024 * 1024 * 100)
                .execute();

        try (FileOutputStream out = new FileOutputStream(updateFileName)) {
            out.write(response.bodyAsBytes());
        }
        // Only check the hash if the user hasn't disabled hash checking
        if (Utils.getConfigBoolean("security.check_update_hash", true)) {
            String updateHash = createSha256(new File(updateFileName));
            logger.info("Download of new version complete; saved to " + updateFileName);
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
        if (shouldLaunch) {
            // Setup updater script
            final String batchFile, script;
            final String[] batchExec;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows
                batchFile = "update_ripme.bat";
                String batchPath = new File(batchFile).getAbsolutePath();
                script = "@echo off\r\n"
                        + "timeout 1" + "\r\n"
                        + "copy " + updateFileName + " " + mainFileName + "\r\n"
                        + "del " + updateFileName + "\r\n"
                        + "ripme.jar" + "\r\n"
                        + "del " + batchPath + "\r\n";
                batchExec = new String[]{batchPath};

            } else {
                // Mac / Linux
                batchFile = "update_ripme.sh";
                String batchPath = new File(batchFile).getAbsolutePath();
                script = "#!/bin/sh\n"
                        + "sleep 1" + "\n"
                        + "cd " + new File(mainFileName).getAbsoluteFile().getParent() + "\n"
                        + "cp -f " + updateFileName + " " + mainFileName + "\n"
                        + "rm -f " + updateFileName + "\n"
                        + "java -jar \"" + new File(mainFileName).getAbsolutePath() + "\" &\n"
                        + "sleep 1" + "\n"
                        + "rm -f " + batchPath + "\n";
                batchExec = new String[]{"sh", batchPath};
            }

            // Create updater script
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(batchFile))) {
                bw.write(script);
                bw.flush();
            }

            logger.info("Saved update script to " + batchFile);
            // Run updater script on exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Executing: " + batchFile);
                    Runtime.getRuntime().exec(batchExec);
                } catch (IOException e) {
                    //TODO implement proper stack trace handling this is really just intented as a placeholder until you implement proper error handling
                    e.printStackTrace();
                }
            }));
            logger.info("Exiting older version, should execute update script (" + batchFile + ") during exit");
            System.exit(0);
        } else {
            new File(mainFileName).delete();
            new File(updateFileName).renameTo(new File(mainFileName));
        }
    }

}
