package com.rarchives.ripme.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.utils.Utils;

public class UpdateUtils {

    private static final Logger logger = Logger.getLogger(UpdateUtils.class);
    private static final String DEFAULT_VERSION = "1.4.14";
    private static final String updateJsonURL = "https://raw.githubusercontent.com/4pr0n/ripme/master/ripme.json";
    private static final String mainFileName = "ripme.jar";
    private static final String updateFileName = "ripme.jar.update";

    public static String getUpdateJarURL(String latestVersion) {
        return "https://github.com/4pr0n/ripme/releases/download/" + latestVersion + "/ripme.jar";
    }

    public static String getThisJarVersion() {
        String thisVersion = UpdateUtils.class.getPackage().getImplementationVersion();
        if (thisVersion == null) {
            // Version is null if we're not running from the JAR
            thisVersion = DEFAULT_VERSION; // Super-high version number
        }
        return thisVersion;
    }

    public static void updateProgram(JLabel configUpdateLabel) {
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
        JSONObject json = new JSONObject(jsonString);
        JSONArray jsonChangeList = json.getJSONArray("changeList");
        StringBuilder changeList = new StringBuilder();
        for (int i = 0; i < jsonChangeList.length(); i++) {
            String change = jsonChangeList.getString(i);
            if (change.startsWith(UpdateUtils.getThisJarVersion() + ":")) {
                break;
            }
            changeList.append("<br>  + ").append(change);
        }

        String latestVersion = json.getString("latestVersion");
        if (UpdateUtils.isNewerVersion(latestVersion)) {
            logger.info("Found newer version: " + latestVersion);
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "<html><font color=\"green\">New version (" + latestVersion + ") is available!</font>"
                    + "<br><br>Recent changes:" + changeList.toString()
                    + "<br><br>Do you want to download and run the newest version?</html>",
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
                UpdateUtils.downloadJarAndLaunch(getUpdateJarURL(latestVersion));
            } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error while updating: " + e.getMessage(),
                    "RipMe Updater",
                    JOptionPane.ERROR_MESSAGE);
            configUpdateLabel.setText("");
                logger.error("Error while updating: ", e);
                return;
            }
        } else {
            logger.debug("This version (" + UpdateUtils.getThisJarVersion() +
                        ") is the same or newer than the website's version (" + latestVersion + ")");
            configUpdateLabel.setText("<html><font color=\"green\">v" + UpdateUtils.getThisJarVersion() + " is the latest version</font></html>");
            logger.debug("Running latest version: " + UpdateUtils.getThisJarVersion());
        }
    }

    private static boolean isNewerVersion(String latestVersion) {
        int[] oldVersions = versionStringToInt(getThisJarVersion());
        int[] newVersions = versionStringToInt(latestVersion);
        if (oldVersions.length < newVersions.length) {
            System.err.println("Calculated: " + getThisJarVersion() + " < " + latestVersion);
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

    private static void downloadJarAndLaunch(String updateJarURL)
            throws IOException {
        Response response;
        response = Jsoup.connect(updateJarURL)
                .ignoreContentType(true)
                .timeout(Utils.getConfigInteger("download.timeout", 60 * 1000))
                .maxBodySize(1024 * 1024 * 100)
                .execute();
        FileOutputStream out = new FileOutputStream(updateFileName);
        out.write(response.bodyAsBytes());
        out.close();
        logger.info("Download of new version complete; saved to " + updateFileName);

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
            batchExec = new String[] { batchPath };

        }
        else {
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
            batchExec = new String[] { "sh", batchPath };
        }
        // Create updater script
        BufferedWriter bw = new BufferedWriter(new FileWriter(batchFile));
        bw.write(script);
        bw.flush();
        bw.close();
        logger.info("Saved update script to " + batchFile);
        // Run updater script on exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    logger.info("Executing: " + batchFile);
                    Runtime.getRuntime().exec(batchExec);
                } catch (IOException e) {
                    //TODO implement proper stack trace handling this is really just intented as a placeholder until you implement proper error handling
                    e.printStackTrace();
                }
            }
        });
        logger.info("Exiting older version, should execute update script (" + batchFile + ") during exit");
        System.exit(0);
    }

}
