package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class ImgurRipper extends AlbumRipper {

    private static final String DOMAIN = "imgur.com",
                                HOST   = "imgur";

    private final int SLEEP_BETWEEN_ALBUMS;

    private Document albumDoc;

    static enum ALBUM_TYPE {
        ALBUM,
        USER,
        USER_ALBUM,
        USER_IMAGES,
        SERIES_OF_IMAGES,
        SUBREDDIT
    };
    private ALBUM_TYPE albumType;

    public ImgurRipper(URL url) throws IOException {
        super(url);
        SLEEP_BETWEEN_ALBUMS = 1;
    }

    /**
     * Imgur ripper does not return the same URL except when ripping
     * many albums at once (USER). In this case, we want duplicates.
     */
    @Override
    public boolean allowDuplicates() {
        return albumType == ALBUM_TYPE.USER;
    }

    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
           return false;
        }
        try {
            getGID(url);
        } catch (Exception e) {
            // Can't get GID, can't rip it.
            return false;
        }
        return true;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.indexOf('#') >= 0) {
            u = u.substring(0, u.indexOf('#'));
        }
        u = u.replace("imgur.com/gallery/", "imgur.com/a/");
        u = u.replace("https?://m\\.imgur\\.com", "http://imgur.com");
        u = u.replace("https?://i\\.imgur\\.com", "http://imgur.com");
        return new URL(u);
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        String gid = getGID(url);
        if (this.albumType == ALBUM_TYPE.ALBUM) {
            try {
                // Attempt to use album title as GID
                if (albumDoc == null) {
                    albumDoc = Http.url(url).get();
                }

                Elements elems = null;

                /*
                // TODO: Add config option for including username in album title.
                // It's possible a lot of users would not be interested in that info.
                String user = null;
                elems = albumDoc.select(".post-account");
                if (elems.size() > 0) {
                    Element postAccount = elems.get(0);
                    if (postAccount != null) {
                        user = postAccount.text();
                    }
                }
                */

                String title = null;
                logger.info("Trying to get album title");
                elems = albumDoc.select("meta[property=og:title]");
                if (elems!=null) {
                    title = elems.attr("content");
                }

                String albumTitle = "imgur_";
                /*
                // TODO: Add config option (see above)
                if (user != null) {
                    albumTitle += "user_" + user;
                }
                */
                albumTitle += gid;
                if (title != null) {
                    albumTitle += " (" + title + ")";
                }

                return albumTitle;
            } catch (IOException e) {
                // Fall back to default album naming convention
            }
        }
        return getHost() + "_" + gid;
    }

    @Override
    public void rip() throws IOException {
        switch (albumType) {
        case ALBUM:
            // Fall-through
        case USER_ALBUM:
            logger.info("Album type is USER_ALBUM");
            // Don't call getAlbumTitle(this.url) with this
            // as it seems to cause the album to be downloaded to a subdir.
            ripAlbum(this.url);
            break;
        case SERIES_OF_IMAGES:
            logger.info("Album type is SERIES_OF_IMAGES");
            ripAlbum(this.url);
            break;
        case USER:
            logger.info("Album type is USER");
            ripUserAccount(url);
            break;
        case SUBREDDIT:
            logger.info("Album type is SUBREDDIT");
            ripSubreddit(url);
            break;
        case USER_IMAGES:
            logger.info("Album type is USER_IMAGES");
            ripUserImages(url);
            break;
        }
        waitForThreads();
    }

    private void ripAlbum(URL url) throws IOException {
        ripAlbum(url, "");
    }

    private void ripAlbum(URL url, String subdirectory) throws IOException {
        int index = 0;
        this.sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());
        index = 0;
        ImgurAlbum album = getImgurAlbum(url);
        for (ImgurImage imgurImage : album.images) {
            stopCheck();
            String saveAs = workingDir.getCanonicalPath();
            if (!saveAs.endsWith(File.separator)) {
                saveAs += File.separator;
            }
            if (subdirectory != null && !subdirectory.equals("")) {
                saveAs += subdirectory;
            }
            if (!saveAs.endsWith(File.separator)) {
                saveAs += File.separator;
            }
            File subdirFile = new File(saveAs);
            if (!subdirFile.exists()) {
                subdirFile.mkdirs();
            }
            index += 1;
            if (Utils.getConfigBoolean("download.save_order", true)) {
                saveAs += String.format("%03d_", index);
            }
            saveAs += imgurImage.getSaveAs();
            addURLToDownload(imgurImage.url, new File(saveAs));
        }
    }

    public static ImgurAlbum getImgurSeries(URL url) throws IOException {
        Pattern p = Pattern.compile("^.*imgur\\.com/([a-zA-Z0-9,]*).*$");
        Matcher m = p.matcher(url.toExternalForm());
        ImgurAlbum album = new ImgurAlbum(url);
        if (m.matches()) {
            String[] imageIds = m.group(1).split(",");
            for (String imageId : imageIds) {
                // TODO: Fetch image with ID imageId
                logger.debug("Fetching image info for ID " + imageId);;
                try {
                    JSONObject json = Http.url("https://api.imgur.com/2/image/" + imageId + ".json").getJSON();
                    if (!json.has("image")) {
                        continue;
                    }
                    JSONObject image = json.getJSONObject("image");
                    if (!image.has("links")) {
                        continue;
                    }
                    JSONObject links = image.getJSONObject("links");
                    if (!links.has("original")) {
                        continue;
                    }
                    String original = links.getString("original");
                    ImgurImage theImage = new ImgurImage(new URL(original));
                    album.addImage(theImage);
                } catch (Exception e) {
                    logger.error("Got exception while fetching imgur ID " + imageId, e);
                }
            }
        }
        return album;
    }

    public static ImgurAlbum getImgurAlbum(URL url) throws IOException {
        String strUrl = url.toExternalForm();
        if (!strUrl.contains(",")) {
            strUrl += "/all";
        }
        logger.info("    Retrieving " + strUrl);
        Document doc = Jsoup.connect(strUrl)
                            .userAgent(USER_AGENT)
                            .timeout(10 * 1000)
                            .maxBodySize(0)
                            .get();

        // Try to use embedded JSON to retrieve images
        Pattern p = Pattern.compile("^.*Imgur\\.Album\\.getInstance\\((.*?)\\);.*$", Pattern.DOTALL);
        Matcher m = p.matcher(doc.body().html());
        if (m.matches()) {
            try {
                JSONObject json = new JSONObject(m.group(1));
                JSONObject jsonAlbum = json.getJSONObject("album");
                ImgurAlbum imgurAlbum = new ImgurAlbum(url, jsonAlbum.getString("title_clean"));
                JSONArray images = json.getJSONObject("images").getJSONArray("images");
                int imagesLength = images.length();
                for (int i = 0; i < imagesLength; i++) {
                    JSONObject image = images.getJSONObject(i);
                    String ext = image.getString("ext");
                    if (ext.equals(".gif") && Utils.getConfigBoolean("prefer.mp4", false)) {
                        ext = ".mp4";
                    }
                    URL imageURL = new URL(
                            // CDN url is provided elsewhere in the document
                            "http://i.imgur.com/"
                                    + image.get("hash")
                                    + ext);
                    String title = null, description = null;
                    if (image.has("title") && !image.isNull("title")) {
                        title = image.getString("title");
                    }
                    if (image.has("description") && !image.isNull("description")) {
                        description = image.getString("description");
                    }
                    ImgurImage imgurImage =  new ImgurImage(imageURL,
                            title,
                            description);
                    imgurAlbum.addImage(imgurImage);
                }
                return imgurAlbum;
            } catch (JSONException e) {
                logger.debug("Error while parsing JSON at " + strUrl + ", continuing", e);
            }
        }
        p = Pattern.compile("^.*widgetFactory.mergeConfig\\('gallery', (.*?)\\);.*$", Pattern.DOTALL);
        m = p.matcher(doc.body().html());
        if (m.matches()) {
            try {
                ImgurAlbum imgurAlbum = new ImgurAlbum(url);
                JSONObject json = new JSONObject(m.group(1));
                JSONArray images = json.getJSONObject("image")
                                       .getJSONObject("album_images")
                                       .getJSONArray("images");
                int imagesLength = images.length();
                for (int i = 0; i < imagesLength; i++) {
                    JSONObject image = images.getJSONObject(i);
                    String ext = image.getString("ext");
                    if (ext.equals(".gif") && Utils.getConfigBoolean("prefer.mp4", false)) {
                        ext = ".mp4";
                    }
                    URL imageURL = new URL(
                            "http://i.imgur.com/"
                                    + image.getString("hash")
                                    + ext);
                    ImgurImage imgurImage = new ImgurImage(imageURL);
                    imgurImage.extension = ext;
                    imgurAlbum.addImage(imgurImage);
                }
                return imgurAlbum;
            } catch (JSONException e) {
                logger.debug("Error while parsing JSON at " + url + ", continuing", e);
            }
        }

        // TODO If album is empty, use this to check for cached images:
        // http://i.rarchives.com/search.cgi?cache=http://imgur.com/a/albumID
        // At the least, get the thumbnails.

        logger.info("[!] Falling back to /noscript method");

        String newUrl = url.toExternalForm() + "/noscript";
        logger.info("    Retrieving " + newUrl);
        doc = Jsoup.connect(newUrl)
                            .userAgent(USER_AGENT)
                            .get();

        // Fall back to parsing HTML elements
        // NOTE: This does not always get the highest-resolution images!
        ImgurAlbum imgurAlbum = new ImgurAlbum(url);
        for (Element thumb : doc.select("div.image")) {
            String image;
            if (thumb.select("a.zoom").size() > 0) {
                // Clickably full-size
                image = "http:" + thumb.select("a").attr("href");
            } else if (thumb.select("img").size() > 0) {
                image = "http:" + thumb.select("img").attr("src");
            } else {
                // Unable to find image in this div
                logger.error("[!] Unable to find image in div: " + thumb.toString());
                continue;
            }
            if (image.endsWith(".gif") && Utils.getConfigBoolean("prefer.mp4", false)) {
                image = image.replace(".gif", ".mp4");
            }
            ImgurImage imgurImage = new ImgurImage(new URL(image));
            imgurAlbum.addImage(imgurImage);
        }
        return imgurAlbum;
    }

    /**
     * Rips all albums in an imgur user's account.
     * @param url
     *      URL to imgur user account (http://username.imgur.com)
     * @throws IOException
     */
    private void ripUserAccount(URL url) throws IOException {
        logger.info("Retrieving " + url);
        sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());
        Document doc = Http.url(url).get();
        for (Element album : doc.select("div.cover a")) {
            stopCheck();
            if (!album.hasAttr("href")
                    || !album.attr("href").contains("imgur.com/a/")) {
                continue;
            }
            String albumID = album.attr("href").substring(album.attr("href").lastIndexOf('/') + 1);
            URL albumURL = new URL("http:" + album.attr("href") + "/noscript");
            try {
                ripAlbum(albumURL, albumID);
                Thread.sleep(SLEEP_BETWEEN_ALBUMS * 1000);
            } catch (Exception e) {
                logger.error("Error while ripping album: " + e.getMessage(), e);
                continue;
            }
        }
    }

    private void ripUserImages(URL url) throws IOException {
        int page = 0; int imagesFound = 0; int imagesTotal = 0;
        String jsonUrl = url.toExternalForm().replace("/all", "/ajax/images");
        if (jsonUrl.contains("#")) {
            jsonUrl = jsonUrl.substring(0, jsonUrl.indexOf("#"));
        }

        while (true) {
            try {
                page++;
                String jsonUrlWithParams = jsonUrl + "?sort=0&order=1&album=0&page=" + page + "&perPage=60";
                JSONObject json = Http.url(jsonUrlWithParams).getJSON();
                JSONObject jsonData = json.getJSONObject("data");
                if (jsonData.has("count")) {
                    imagesTotal = jsonData.getInt("count");
                }
                JSONArray images = jsonData.getJSONArray("images");
                for (int i = 0; i < images.length(); i++) {
                    imagesFound++;
                    JSONObject image = images.getJSONObject(i);
                    String imageUrl = "http://i.imgur.com/" + image.getString("hash") + image.getString("ext");
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", imagesFound);
                    }
                    addURLToDownload(new URL(imageUrl), prefix);
                }
                if (imagesFound >= imagesTotal) {
                    break;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("Error while ripping user images: " + e.getMessage(), e);
                break;
            }
        }
    }

    private void ripSubreddit(URL url) throws IOException {
        int page = 0;
        while (true) {
            stopCheck();
            String pageURL = url.toExternalForm();
            if (!pageURL.endsWith("/")) {
                pageURL += "/";
            }
            pageURL += "page/" + page + "/miss?scrolled";
            logger.info("    Retrieving " + pageURL);
            Document doc = Http.url(pageURL).get();
            Elements imgs = doc.select(".post img");
            for (Element img : imgs) {
                String image = img.attr("src");
                if (image.startsWith("//")) {
                    image = "http:" + image;
                }
                if (image.contains("b.")) {
                    image = image.replace("b.", ".");
                }
                URL imageURL = new URL(image);
                addURLToDownload(imageURL);
            }
            if (imgs.size() == 0) {
                break;
            }
            page++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to load next album: ", e);
                break;
            }
        }
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = null;
        Matcher m = null;

        p = Pattern.compile("^https?://(www\\.|m\\.)?imgur\\.com/(a|gallery)/([a-zA-Z0-9]{5,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur album or gallery
            albumType = ALBUM_TYPE.ALBUM;
            String gid = m.group(m.groupCount());
            this.url = new URL("http://imgur.com/a/" + gid);
            return gid;
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{3,})\\.imgur\\.com/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Root imgur account
            String gid = m.group(1);
            if (gid.equals("www")) {
                throw new MalformedURLException("Cannot rip the www.imgur.com homepage");
            }
            albumType = ALBUM_TYPE.USER;
            return "user_" + gid;
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{3,})\\.imgur\\.com/all.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur account images
            albumType = ALBUM_TYPE.USER_IMAGES;
            return m.group(1) + "_images";
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{3,})\\.imgur\\.com/([a-zA-Z0-9\\-_]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur account album
            albumType = ALBUM_TYPE.USER_ALBUM;
            return m.group(1) + "-" + m.group(2);
        }
        p = Pattern.compile("^https?://(www\\.|m\\.)?imgur\\.com/r/([a-zA-Z0-9\\-_]{3,})(/top|/new)?(/all|/year|/month|/week|/day)?/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur subreddit aggregator
            albumType = ALBUM_TYPE.SUBREDDIT;
            String album = m.group(2);
            for (int i = 3; i <= m.groupCount(); i++) {
                if (m.group(i) != null) {
                    album += "_" + m.group(i).replace("/", "");
                }
            }
            return album;
        }
        p = Pattern.compile("^https?://(i\\.|www\\.|m\\.)?imgur\\.com/r/(\\w+)/([a-zA-Z0-9,]{5,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur subreddit album or image (treat as album)
            albumType = ALBUM_TYPE.ALBUM;
            String subreddit = m.group(m.groupCount() - 1);
            String gid = m.group(m.groupCount());
            this.url = new URL("http://imgur.com/r/" + subreddit + "/" + gid);
            return "r_" + subreddit + "_" + gid;
        }
        p = Pattern.compile("^https?://(i\\.|www\\.|m\\.)?imgur\\.com/([a-zA-Z0-9,]{5,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Series of imgur images
            albumType = ALBUM_TYPE.SERIES_OF_IMAGES;
            String gid = m.group(m.groupCount());
            if (!gid.contains(",")) {
                throw new MalformedURLException("Imgur image doesn't contain commas");
            }
            return gid.replaceAll(",", "-");
        }
        throw new MalformedURLException("Unsupported imgur URL format: " + url.toExternalForm());
    }

    public ALBUM_TYPE getAlbumType() {
        return albumType;
    }

    public static class ImgurImage {
        public String title = "",
                description = "",
                extension   = "";
        public URL url = null;

        public ImgurImage(URL url) {
            this.url = url;
            String tempUrl = url.toExternalForm();
            this.extension = tempUrl.substring(tempUrl.lastIndexOf('.'));
            if (this.extension.contains("?")) {
                this.extension = this.extension.substring(0, this.extension.indexOf("?"));
            }
        }
        public ImgurImage(URL url, String title) {
            this(url);
            this.title = title;
        }
        public ImgurImage(URL url, String title, String description) {
            this(url, title);
            this.description = description;
        }
        public String getSaveAs() {
            String saveAs = this.title;
            String u = url.toExternalForm();
            if (u.contains("?")) {
                u = u.substring(0, u.indexOf("?"));
            }
            String imgId = u.substring(u.lastIndexOf('/') + 1, u.lastIndexOf('.'));
            if (saveAs == null || saveAs.equals("")) {
                saveAs = imgId;
            } else {
                saveAs = saveAs + "_" + imgId;
            }
            saveAs = Utils.filesystemSafe(saveAs);
            return saveAs + this.extension;
        }
    }

    public static class ImgurAlbum {
        public String title = null;
        public URL    url = null;
        public List<ImgurImage> images = new ArrayList<ImgurImage>();
        public ImgurAlbum(URL url) {
            this.url = url;
        }
        public ImgurAlbum(URL url, String title) {
            this(url);
            this.title = title;
        }
        public void addImage(ImgurImage image) {
            images.add(image);
        }
    }

}
