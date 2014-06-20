package com.rarchives.ripme.ripper.rippers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class MediacrushRipper extends AlbumRipper {

    private static final String DOMAIN = "mediacru.sh",
                                HOST   = "mediacrush";
    
    /** Ordered list of preferred formats, sorted by preference (low-to-high) */
    private static final Map<String, Integer> PREFERRED_FORMATS = new HashMap<String,Integer>();
    static {
        PREFERRED_FORMATS.put("mp4", 0);
        PREFERRED_FORMATS.put("wemb",1);
        PREFERRED_FORMATS.put("ogv", 2);
        PREFERRED_FORMATS.put("mp3", 3);
        PREFERRED_FORMATS.put("ogg", 4);
        PREFERRED_FORMATS.put("gif", 5);
        PREFERRED_FORMATS.put("png", 6);
        PREFERRED_FORMATS.put("jpg", 7);
        PREFERRED_FORMATS.put("jpeg",8);
    };

    public MediacrushRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        // Strip trailing "/" characters
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        // Append .json
        if (!u.endsWith(".json")) {
            u += ".json";
        }
        return new URL(u);
    }

    @Override
    public void rip() throws IOException {
        String url = this.url.toExternalForm();
        logger.info("    Retrieving " + url);
        sendUpdate(STATUS.LOADING_RESOURCE, url);
        String jsonString = null;
        try {
            jsonString = getResponse(url, true).body();
        } catch (Exception re) {
            // Check for >1024 bit encryption but in older versions of Java
            if (re.getCause().getCause() instanceof InvalidAlgorithmParameterException) {
                // It's the bug. Suggest downloading the latest version.
                int selection = JOptionPane.showOptionDialog(null,
                        "You need to upgrade to the latest Java (7+) to rip this album.\n"
                      + "Do you want to open java.com and download the latest version?",
                        "RipMe - Java Error",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        new String[] {"Go to java.com", "Cancel"},
                        0);
                sendUpdate(STATUS.RIP_ERRORED, "Your version of Java can't handle some secure websites");
                if (selection == 0) {
                    URL javaUrl = new URL("https://www.java.com/en/download/");
                    try {
                        Desktop.getDesktop().browse(javaUrl.toURI());
                    } catch (URISyntaxException use) { }
                }
                return;
            }
            throw new IOException("Unexpected error occurred", re);
        }

        // Convert to JSON
        JSONObject json = new JSONObject(jsonString);
        if (!json.has("files")) {
            sendUpdate(STATUS.RIP_ERRORED, "No files found at " + url);
            throw new IOException("Could not find any files at " + url);
        }
        // Iterate over all files
        JSONArray files = json.getJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            JSONObject file = (JSONObject) files.get(i);
            // Find preferred file format
            JSONArray subfiles = file.getJSONArray("files");
            String preferredUrl = getPreferredUrl(subfiles);
            if (preferredUrl == null) {
                sendUpdate(STATUS.DOWNLOAD_ERRORED, "Could not find file inside of " + file);
                continue;
            }

            // Download
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", i + 1);
            }
            addURLToDownload(new URL(preferredUrl), prefix);
        }
        waitForThreads();
    }
    
    /**
     * Iterates over list if "file" objects and returns the preferred 
     * image format.
     * @param subfiles Array of "files" (JSONObjects) which contain 
     * @return Preferred media format.
     */
    private String getPreferredUrl(JSONArray subfiles) {
        String preferredUrl = null;
        int preferredIndex = Integer.MAX_VALUE;
        // Iterate over all media types
        for (int j = 0; j < subfiles.length(); j++) {
            JSONObject subfile = subfiles.getJSONObject(j);
            String thisurl = subfile.getString("url");
            String extension = thisurl.substring(thisurl.lastIndexOf(".") + 1);
            if (!PREFERRED_FORMATS.containsKey(extension)) {
                continue;
            }
            // Keep track of the most-preferred format
            int thisindex = PREFERRED_FORMATS.get(extension);
            if (preferredUrl == null || thisindex < preferredIndex) {
                preferredIndex = thisindex;
                preferredUrl = thisurl;
            }
        }
        return preferredUrl;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://[wm.]*mediacru\\.sh/([a-zA-Z0-9]+).*");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Could not find mediacru.sh page ID from " + url
                 + " expected format: http://mediacru.sh/pageid");
    }
}
