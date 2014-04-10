package com.rarchives.ripme.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UpdateUtils {

    private static final Logger logger = Logger.getLogger(UpdateUtils.class);
    private static final String DEFAULT_VERSION = "1.0.14";
    private static final String updateJsonURL = "http://rarchives.com/ripme.json";
    private static final String updateJarURL = "http://rarchives.com/ripme.jar";
    private static final String mainFileName = "ripme.jar";
    private static final String updateFileName = "ripme.jar.update";

    public static String getThisJarVersion() {
        String thisVersion = UpdateUtils.class.getPackage().getImplementationVersion();
        if (thisVersion == null) {
            // Version is null if we're not running from the JAR
            thisVersion = DEFAULT_VERSION; ; // Super-high version number
        }
        return thisVersion;
    }
    
    public static void updateProgram(JLabel configUpdateLabel) {
        configUpdateLabel.setText("Checking for update...");
        
        Document doc = null;
        try {
            doc = Jsoup.connect(UpdateUtils.updateJsonURL)
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
            changeList.append("<br>  + " + change);
        }

        String latestVersion = json.getString("latestVersion");
        if (UpdateUtils.isNewerVersion(latestVersion)) {
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
                UpdateUtils.downloadJarAndLaunch(updateJarURL);
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
            configUpdateLabel.setText("<html><font color=\"green\">v" + UpdateUtils.getThisJarVersion() + " is the latest version</font></html>");
            logger.info("Running latest version: " + UpdateUtils.getThisJarVersion());
        }
    }
    
    private static boolean isNewerVersion(String latestVersion) {
        int[] oldVersions = versionStringToInt(getThisJarVersion());
        int[] newVersions = versionStringToInt(latestVersion);
        if (oldVersions.length < newVersions.length) {
            System.err.println("Calculated: " + oldVersions + " < " + latestVersion);
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
            else {
                continue;
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
                .timeout(60000)
                .maxBodySize(1024 * 1024 * 100)
                .execute();
        FileOutputStream out = new FileOutputStream(updateFileName);
        out.write(response.bodyAsBytes());
        out.close();
        logger.info("Download of new version complete; saved to " + updateFileName);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    logger.info("Executing: java -jar " + updateFileName);
                    Runtime.getRuntime().exec(new String[] {"java", "-jar", updateFileName});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        logger.info("Exiting older version, should execute updated jar (" + updateFileName + ") during exit");
        System.exit(0);
    }
    
    public static void moveUpdatedJar() {
        // Copy the new file (ripme.jar.update) to original location (ripme.jar)
        // Delete new file (update) on exit
        // Exit
        File newFile = new File(updateFileName); // ripme.jar.update
        File oldFile = new File(mainFileName);   // ripme.jar
        if (!newFile.exists()) {
            // Can't update without .update file
            return;
        }

        // Attempt to copy new .jar file over old jar file.
        int retries = 3;
        while (true) {
            retries--;
            try {
                logger.info("Updated .jar file '" + newFile + "' exists, overwriting older version at " + oldFile + " ...");
                FileUtils.copyFile(newFile, oldFile);
                break; // Copy was successful; break.
            }
            catch (IOException e) {
                logger.error("Failed to copy the updated jar over the original jar.\nUpdated Jar:\t" + newFile + "\nOriginal Jar: " + oldFile);
                if (retries < 0) {
                    // We failed!
                    // Show error messages, pop up message dialog, and open the directory containing jars
                    logger.error("Cannot ovewrite existing jar file " + oldFile + " with updated file " + newFile + " ... Please update by moving files manually");
                    try {
                        Desktop.getDesktop().open(newFile.getParentFile());
                    } catch (IOException ioe) {
                        logger.error("Error while opening directory " + newFile.getParentFile(), ioe);
                    }
                    JOptionPane.showMessageDialog(null,
                            "Failed to copy the updated .jar file over the original .jar file\nUpdated Jar:\t" + newFile + "\nOriginal Jar: " + oldFile + "\n\nPlease update by moving files manually",
                            "RipMe Updater Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                    return;
                }
                e.printStackTrace();
                try {
                    logger.warn("Waiting 1 second, copy retries remaining: " + retries);
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    logger.error("Interrupted while waiting for original jar " + oldFile + " to be overwritten", ie);
                    return;
                }
            }
        }

        // Delete the updated .jar on exit
        if (newFile.exists()) {
            logger.info("Will delete '" + newFile + "' on exit");
            try {
                FileUtils.forceDeleteOnExit(newFile);
            }
            catch (Exception e) {
                logger.error("Failed to schedule delete on file: " + newFile);
                return;
            }
        }

        // Execute the copied, updated .jar at ripme.jar
        try {
            String[] command = new String[] {
                    "java",
                    "-jar",
                    oldFile.getName() };
            logger.info("Executing: " + command[0] + " " + command[1] + " " + command[2]);
            Runtime.getRuntime().exec(command);
            logger.info("Started new version at " + oldFile.getName() + ", quitting current program...");
            System.exit(0);
        }
        catch (IOException e) {
            logger.error("Error while executing new jar '" + oldFile.getName() + "'", e);
            return;
        }
    }

}
