package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurAlbum;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurImage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class IrarchivesRipper extends AlbumRipper {

    private static final int TIMEOUT = 60000; // Long timeout for this poorly-optimized site.

    private static final String DOMAIN = "i.rarchives.com",
                                HOST   = "irarchives";
    private static final Logger logger = Logger.getLogger(IrarchivesRipper.class);
    
    public IrarchivesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        String searchTerm = u.substring(u.indexOf("?") + 1);
        if (searchTerm.startsWith("url=")) {
            if (!searchTerm.contains("http")
                    && !searchTerm.contains(":")) {
                searchTerm = searchTerm.replace("url=", "user=");
            }
        }
        return new URL("http://i.rarchives.com/search.cgi?" + searchTerm);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url);
        String jsonString = Jsoup.connect(this.url.toExternalForm())
                .ignoreContentType(true)
                .timeout(TIMEOUT)
                .execute()
                .body();
        JSONObject json = new JSONObject(jsonString);
        JSONArray posts = json.getJSONArray("posts");
        if (posts.length() == 0) {
            logger.error("No posts found at " + this.url);
            sendUpdate(STATUS.DOWNLOAD_ERRORED, "No posts found at " + this.url);
            throw new IOException("No posts found at this URL");
        }
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = (JSONObject) posts.get(i);
            String theUrl = post.getString("url");
            if (theUrl.contains("imgur.com/a/")) {
                ImgurAlbum album = null;
                try {
                    album = ImgurRipper.getImgurAlbum(new URL(theUrl));
                } catch (IOException e) {
                    logger.error("Error loading imgur album " + theUrl, e);
                    sendUpdate(STATUS.DOWNLOAD_ERRORED, "Can't download " + theUrl + " : " + e.getMessage());
                    continue;
                }
                int albumIndex = 0;
                for (ImgurImage image : album.images) {
                    albumIndex++;
                    String saveAs = String.format("%s-", post.getString("hexid"));
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        saveAs += String.format("%03d_", albumIndex);
                    }
                    addURLToDownload(image.url, saveAs);
                }
            }
            else {
                theUrl = post.getString("imageurl");
                String saveAs = String.format("%s-", post.getString("hexid"));
                addURLToDownload(new URL(theUrl), saveAs);
            }
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        String searchTerm = u.substring(u.indexOf("?") + 1);
        return Utils.filesystemSafe(searchTerm);
    }
}
