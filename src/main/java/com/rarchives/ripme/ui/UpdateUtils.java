package com.rarchives.ripme.ui;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JLabel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UpdateUtils {

    private static final String DEFAULT_VERSION = "1.0.0";
    private static final String updateJsonURL = "http://rarchives.com/ripme.json";
    private static final String updateJarURL = "http://rarchives.com/ripme.jar";

    public static void updateProgram(JLabel configUpdateLabel) {
        configUpdateLabel.setText("Checking for update...:");
        
        Document doc = null;
        try {
            doc = Jsoup.connect(UpdateUtils.updateJsonURL)
                .ignoreContentType(true)
                .get();
        } catch (IOException e) {
            configUpdateLabel.setText("Error while fetching update: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        String jsonString = doc.body().html().replaceAll("&quot;", "\"");
        JSONObject json = new JSONObject(jsonString);
        JSONArray jsonChangeList = json.getJSONArray("changeList");
        configUpdateLabel.setText("Most recent changes:");
        for (int i = 0; i < jsonChangeList.length(); i++) {
            String change = jsonChangeList.getString(i);
            configUpdateLabel.setText(configUpdateLabel.getText() + "<br>  + " + change);
        }

        String latestVersion = json.getString("latestVersion");
        if (UpdateUtils.isNewerVersion(latestVersion)) {
            configUpdateLabel.setText("<html>Newer version found! <br><br>" + configUpdateLabel.getText() + "</html>");
            try {
                UpdateUtils.downloadJarAndReplace(updateJarURL);
            } catch (IOException e) {
                configUpdateLabel.setText("Error while updating: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            configUpdateLabel.setText("Running latest version: " + UpdateUtils.getThisJarVersion());
        }
    }
    
    private static void downloadJarAndReplace(String updateJarURL)
            throws IOException {
        String newFile = "ripme.jar.update";
        Response response;
        response = Jsoup.connect(updateJarURL)
                .ignoreContentType(true)
                .timeout(60000)
                .maxBodySize(1024 * 1024 * 100)
                .execute();
        FileOutputStream out = new FileOutputStream(newFile);
        out.write(response.bodyAsBytes());
        out.close();
        Runtime.getRuntime().exec(new String[] {"java", "-jar", newFile});
        System.exit(0);
    }
    
    private static String getThisJarVersion() {
        String thisVersion = UpdateUtils.class.getPackage().getImplementationVersion();
        if (thisVersion == null) {
            // Version is null if we're not running from the JAR
            thisVersion = DEFAULT_VERSION; ; // Super-high version number
        }
        return thisVersion;
    }
    
    private static boolean isNewerVersion(String latestVersion) {
        int[] oldVersions = versionStringToInt(getThisJarVersion());
        int[] newVersions = versionStringToInt(latestVersion);
        if (oldVersions.length < newVersions.length) {
            System.err.println("Calculated: " + oldVersions + " < " + latestVersion);
            return true;
        }

        for (int i = 0; i < oldVersions.length; i++) {
            System.err.println("Calculating: " + newVersions[i] + " <> " + oldVersions[i]);
            if (newVersions[i] > oldVersions[i]) {
                return true;
            }
            else if (newVersions[i] < oldVersions[i]) {
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
}
