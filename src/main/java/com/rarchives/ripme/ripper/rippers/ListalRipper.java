package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;



/**
 * @author Tushar
 *
 */
public class ListalRipper extends AbstractHTMLRipper {

    private Pattern p1 = Pattern.compile("https:\\/\\/www.listal.com\\/list\\/([a-zA-Z0-9-]+)");
    private Pattern p2 =
            Pattern.compile("https:\\/\\/www.listal.com\\/((?:(?:[a-zA-Z0-9-_%]+)\\/?)+)");
    private String listId = null; // listId to get more images via POST.
    private String postUrl = "https://www.listal.com/item-list/"; //to load more images.
    private UrlType urlType = UrlType.UNKNOWN;

    private DownloadThreadPool listalThreadPool = new DownloadThreadPool("listalThreadPool");

    public ListalRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "listal.com";
    }

    @Override
    public String getHost() {
        return "listal";
    }

    @Override
    public Document getFirstPage() throws IOException {
        Document doc = Http.url(url).get();
        if (urlType == UrlType.LIST) {
            listId = doc.select("#customlistitems").first().attr("data-listid"); // Used for list types.
        }
        return doc;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        if (urlType == UrlType.LIST) {
            // for url of type LIST, https://www.listal.com/list/my-list 
            return getURLsForListType(page);
        } else if (urlType == UrlType.FOLDER) {
            // for url of type FOLDER,  https://www.listal.com/jim-carrey/pictures
            return getURLsForFolderType(page);
        }
        return null;
    }

    @Override
    public void downloadURL(URL url, int index) {
        listalThreadPool.addThread(new ListalImageDownloadThread(url, index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m1 = p1.matcher(url.toExternalForm());
        if (m1.matches()) {
            // Return the text contained between () in the regex
            urlType = UrlType.LIST;
            return m1.group(1);
        }

        Matcher m2 = p2.matcher(url.toExternalForm());
        if (m2.matches()) {
            // Return only gid from capturing group of type listal.com/tvOrSomething/dexter/pictures
            urlType = UrlType.FOLDER;
            return getFolderTypeGid(m2.group(1));
        }

        throw new MalformedURLException("Expected listal.com URL format: "
                + "listal.com/list/my-list-name - got " + url + " instead.");
    }

    @Override
    public Document getNextPage(Document page) throws IOException {
        Document nextPage = super.getNextPage(page);
        switch (urlType) {
            case LIST:
                if (!page.select(".loadmoreitems").isEmpty()) {
                    // All items are not loaded.
                    // Load remaining items using postUrl.

                    String offSet = page.select(".loadmoreitems").last().attr("data-offset");
                    Map<String, String> postParams = new HashMap<>();
                    postParams.put("listid", listId);
                    postParams.put("offset", offSet);
                    try {
                        nextPage = Http.url(postUrl).data(postParams).retries(3).post();
                    } catch (IOException e1) {
                        LOGGER.error("Failed to load more images after " + offSet, e1);
                        throw e1;
                    }
                }
                break;

            case FOLDER:
                Elements pageLinks = page.select(".pages a");
                if (!pageLinks.isEmpty() && pageLinks.last().text().startsWith("Next")) {
                    String nextUrl = pageLinks.last().attr("abs:href");
                    nextPage = Http.url(nextUrl).retries(3).get();
                }
                break;

            case UNKNOWN:
            default:
        }
        return nextPage;
    }


    @Override
    public DownloadThreadPool getThreadPool() {
        return listalThreadPool;
    }

    /**
     * Returns the image urls for UrlType LIST.
     */
    private List<String> getURLsForListType(Document page) {
        List<String> list = new ArrayList<>();
        for (Element e : page.select(".pure-g a[href*=viewimage]")) {
            //list.add("https://www.listal.com" + e.attr("href") + "h");
            list.add(e.attr("abs:href") + "h");
        }

        return list;
    }

    /**
     * Returns the image urls for UrlType FOLDER.
     */
    private List<String> getURLsForFolderType(Document page) {
        List<String> list = new ArrayList<>();
        for (Element e : page.select("#browseimagescontainer .imagewrap-outer a")) {
            list.add(e.attr("abs:href") + "h");
        }
        return list;
    }

    /**
     * Returns the gid for url type listal.com/tvOrSomething/dexter/pictures
     */
    public String getFolderTypeGid(String group) throws MalformedURLException {
        String[] folders = group.split("/");
        try {
            if (folders.length == 2 && folders[1].equals("pictures")) {
                // Url is probably for an actor.
                return folders[0];
            }

            if (folders.length == 3 && folders[2].equals("pictures")) {
                // Url if for a folder(like movies, tv etc).
                Document doc = Http.url(url).get();
                return doc.select(".itemheadingmedium").first().text();
            }

        } catch (Exception e) {
            LOGGER.error(e);
        }
        throw new MalformedURLException("Unable to fetch the gid for given url.");
    }

    private class ListalImageDownloadThread implements Runnable {

        private final URL url;
        private final int index;

        public ListalImageDownloadThread(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            getImage();
        }

        public void getImage() {
            try {
                Document doc = Http.url(url).get();

                String imageUrl = doc.getElementsByClass("pure-img").attr("src");
                if (imageUrl != "") {
                    addURLToDownload(new URL(imageUrl), getPrefix(index), "", null, null,
                            getImageName());
                } else {
                    LOGGER.error("Couldnt find image from url: " + url);
                }
            } catch (IOException e) {
                LOGGER.error("[!] Exception while downloading image: " + url, e);
            }
        }

        public String getImageName() {
            // Returns the image number of the link if possible.
            String name = this.url.toExternalForm();
            try {
                name = name.substring(name.lastIndexOf("/") + 1);
            } catch (Exception e) {
                LOGGER.info("Failed to get name for the image.");
                name = null;
            }
            // Listal stores images as .jpg
            return name + ".jpg";
        }
    }

    private static enum UrlType {
        LIST, FOLDER, UNKNOWN
    }
}
