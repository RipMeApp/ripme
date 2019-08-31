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
import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;



public class ListalRipper extends AbstractHTMLRipper {

    private Pattern p = Pattern.compile("https://www.listal.com/list/([a-zA-Z0-9-]+)");
    private String listId = null; // listId to get more images via POST.
    private String postUrl = "https://www.listal.com/item-list/"; //to load more images.

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
        listId = doc.select("#customlistitems").first().attr("data-listid");
        return doc;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // for url of type https://www.listal.com/list/my-list 
            return getURLsFromList(page);
        } else {
            // for url of type https://www.listal.com/jim-carrey/pictures
            //TODO need to write
            return null;
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        listalThreadPool.addThread(new ListalImageDownloadThread(url, index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://www.listal.com/list/([a-zA-Z0-9-]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Return the text contained between () in the regex
            return m.group(1);
        }

        //TODO match /../celebrity_name/images
        throw new MalformedURLException("Expected listal.com URL format: "
                + "listal.com/list/my-list-name - got " + url + " instead.");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // TODO Auto-generated method stub
        return super.getNextPage(doc);
    }

    private List<String> getURLsFromList(Document page) {
        // recursive method for url type: https://www.listal.com/list/my-list

        List<String> list = new ArrayList<>();
        for (Element e : page.select(".pure-g a[href*=viewimage]")) {
            //list.add("https://www.listal.com" + e.attr("href") + "h");
            list.add(e.attr("abs:href") + "h");
        }

        if (!page.select(".loadmoreitems").isEmpty()) {
            // All items are not loaded.
            // Load remaining items using postUrl.

            String offSet = page.select(".loadmoreitems").last().attr("data-offset");
            Map<String, String> postParams = new HashMap<>();
            postParams.put("listid", listId);
            postParams.put("offset", offSet);
            try {
                list.addAll(getURLsFromList(Http.url(postUrl).data(postParams).retries(3).post()));
            } catch (IOException e1) {
                LOGGER.error("Failed to load more images after " + offSet, e1);
            }
        }

        return list;
    }

    @Override
    public DownloadThreadPool getThreadPool() {
        return listalThreadPool;
    }

    private class ListalImageDownloadThread extends Thread {

        private URL url;
        private int index;

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
                    addURLToDownload(new URL(imageUrl), getPrefix(index));
                } else {
                    LOGGER.error("Couldnt find image from url: " + url);
                }
            } catch (IOException e) {
                LOGGER.error("[!] Exception while downloading image: " + url, e);
            }
        }
    }
}
