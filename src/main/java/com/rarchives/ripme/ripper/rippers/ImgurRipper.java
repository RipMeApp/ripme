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

    enum ALBUM_TYPE {
        ALBUM,
        USER,
        USER_ALBUM,
        USER_IMAGES,
        SINGLE_IMAGE,
        SERIES_OF_IMAGES,
        SUBREDDIT
    }

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
                final String defaultTitle1 = "Imgur: The most awesome images on the Internet";
                final String defaultTitle2 = "Imgur: The magic of the Internet";
                LOGGER.info("Trying to get album title");
                elems = albumDoc.select("meta[property=og:title]");
                if (elems != null) {
                    title = elems.attr("content");
                    LOGGER.debug("Title is " + title);
                }
                // This is here encase the album is unnamed, to prevent
                // Imgur: The most awesome images on the Internet from being added onto the album name
                if (title.contains(defaultTitle1) || title.contains(defaultTitle2)) {
                    LOGGER.debug("Album is untitled or imgur is returning the default title");
                    // We set the title to "" here because if it's found in the next few attempts it will be changed
                    // but if it's nto found there will be no reason to set it later
                    title = "";
                    LOGGER.debug("Trying to use title tag to get title");
                    elems = albumDoc.select("title");
                    if (elems != null) {
                        if (elems.text().contains(defaultTitle1) || elems.text().contains(defaultTitle2)) {
                            LOGGER.debug("Was unable to get album title or album was untitled");
                        }
                        else {
                            title = elems.text();
                        }
                    }
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
                    albumTitle += "_" + title;
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
                LOGGER.info("Album type is USER_ALBUM");
                // Don't call getAlbumTitle(this.url) with this
                // as it seems to cause the album to be downloaded to a subdir.
                ripAlbum(this.url);
                break;
            case SERIES_OF_IMAGES:
                LOGGER.info("Album type is SERIES_OF_IMAGES");
                ripAlbum(this.url);
                break;
            case SINGLE_IMAGE:
                LOGGER.info("Album type is SINGLE_IMAGE");
                ripSingleImage(this.url);
                break;
            case USER:
                LOGGER.info("Album type is USER");
                ripUserAccount(url);
                break;
            case SUBREDDIT:
                LOGGER.info("Album type is SUBREDDIT");
                ripSubreddit(url);
                break;
            case USER_IMAGES:
                LOGGER.info("Album type is USER_IMAGES");
                ripUserImages(url);
                break;
        }
        waitForThreads();
    }

    private void ripSingleImage(URL url) throws IOException {
        String strUrl = url.toExternalForm();
        Document document = getDocument(strUrl);
        Matcher m = getEmbeddedJsonMatcher(document);
        if (m.matches()) {
            JSONObject json = new JSONObject(m.group(1)).getJSONObject("image");
            addURLToDownload(extractImageUrlFromJson(json), "");
        }
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
            saveAs = saveAs.replaceAll("\\?\\d", "");
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
                LOGGER.debug("Fetching image info for ID " + imageId);
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
                    LOGGER.error("Got exception while fetching imgur ID " + imageId, e);
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
        LOGGER.info("    Retrieving " + strUrl);
        Document doc = getDocument(strUrl);
        // Try to use embedded JSON to retrieve images
        Matcher m = getEmbeddedJsonMatcher(doc);
        if (m.matches()) {
            try {
                JSONObject json = new JSONObject(m.group(1));
                JSONArray jsonImages = json.getJSONObject("image")
                                       .getJSONObject("album_images")
                                       .getJSONArray("images");
                return createImgurAlbumFromJsonArray(url, jsonImages);
            } catch (JSONException e) {
                LOGGER.debug("Error while parsing JSON at " + url + ", continuing", e);
            }
        }

        // TODO If album is empty, use this to check for cached images:
        // http://i.rarchives.com/search.cgi?cache=http://imgur.com/a/albumID
        // At the least, get the thumbnails.

        LOGGER.info("[!] Falling back to /noscript method");

        String newUrl = url.toExternalForm() + "/noscript";
        LOGGER.info("    Retrieving " + newUrl);
        doc = Jsoup.connect(newUrl)
                            .userAgent(USER_AGENT)
                            .get();

        // Fall back to parsing HTML elements
        // NOTE: This does not always get the highest-resolution images!
        ImgurAlbum imgurAlbum = new ImgurAlbum(url);
        for (Element thumb : doc.select("div.image")) {
            String image;
            if (!thumb.select("a.zoom").isEmpty()) {
                // Clickably full-size
                image = "http:" + thumb.select("a").attr("href");
            } else if (!thumb.select("img").isEmpty()) {
                image = "http:" + thumb.select("img").attr("src");
            } else {
                // Unable to find image in this div
                LOGGER.error("[!] Unable to find image in div: " + thumb.toString());
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

    private static Matcher getEmbeddedJsonMatcher(Document doc) {
        Pattern p = Pattern.compile("^.*widgetFactory.mergeConfig\\('gallery', (.*?)\\);.*$", Pattern.DOTALL);
        return p.matcher(doc.body().html());
    }

    private static ImgurAlbum createImgurAlbumFromJsonArray(URL url, JSONArray jsonImages) throws MalformedURLException {
        ImgurAlbum imgurAlbum = new ImgurAlbum(url);
        int imagesLength = jsonImages.length();
        for (int i = 0; i < imagesLength; i++) {
            JSONObject jsonImage = jsonImages.getJSONObject(i);
            imgurAlbum.addImage(createImgurImageFromJson(jsonImage));
        }
        return imgurAlbum;
    }

    private static ImgurImage createImgurImageFromJson(JSONObject json) throws MalformedURLException {
        return new ImgurImage(extractImageUrlFromJson(json));
    }

    private static URL extractImageUrlFromJson(JSONObject json) throws MalformedURLException {
        String ext = json.getString("ext");
        if (ext.equals(".gif") && Utils.getConfigBoolean("prefer.mp4", false)) {
            ext = ".mp4";
        }
        return  new URL(
                "http://i.imgur.com/"
                        + json.getString("hash")
                        + ext);
    }

    private static Document getDocument(String strUrl) throws IOException {
        return Jsoup.connect(strUrl)
                                .userAgent(USER_AGENT)
                                .timeout(10 * 1000)
                                .maxBodySize(0)
                                .get();
    }

    /**
     * Rips all albums in an imgur user's account.
     * @param url
     *      URL to imgur user account (http://username.imgur.com)
     * @throws IOException
     */
    private void ripUserAccount(URL url) throws IOException {
        LOGGER.info("Retrieving " + url);
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
                LOGGER.error("Error while ripping album: " + e.getMessage(), e);
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
                LOGGER.error("Error while ripping user images: " + e.getMessage(), e);
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
            LOGGER.info("    Retrieving " + pageURL);
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
            if (imgs.isEmpty()) {
                break;
            }
            page++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting to load next album: ", e);
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
        p = Pattern.compile("^https?://(www\\.|m\\.)?imgur\\.com/(a|gallery|t)/[a-zA-Z0-9]*/([a-zA-Z0-9]{5,}).*$");
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
        p = Pattern.compile("^https?://(i\\.|www\\.|m\\.)?imgur\\.com/([a-zA-Z0-9]{5,})$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Single imgur image
            albumType = ALBUM_TYPE.SINGLE_IMAGE;
            return  m.group(m.groupCount());
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
        String title = "";
        String description = "";
        String extension   = "";
        public URL url = null;

        ImgurImage(URL url) {
            this.url = url;
            String tempUrl = url.toExternalForm();
            this.extension = tempUrl.substring(tempUrl.lastIndexOf('.'));
            if (this.extension.contains("?")) {
                this.extension = this.extension.substring(0, this.extension.indexOf("?"));
            }
        }
        ImgurImage(URL url, String title) {
            this(url);
            this.title = title;
        }
        public ImgurImage(URL url, String title, String description) {
            this(url, title);
            this.description = description;
        }
        String getSaveAs() {
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
        String title = null;
        public URL    url = null;
        public List<ImgurImage> images = new ArrayList<>();
        ImgurAlbum(URL url) {
            this.url = url;
        }
        public ImgurAlbum(URL url, String title) {
            this(url);
            this.title = title;
        }
        void addImage(ImgurImage image) {
            images.add(image);
        }
    }

}
