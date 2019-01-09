package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

import static com.rarchives.ripme.utils.RipUtils.getCookiesFromString;

public class FuraffinityRipper extends AbstractHTMLRipper {

    private static final String urlBase = "https://www.furaffinity.net";
    private  Map<String,String> cookies = new HashMap<>();

    private void setCookies() {
        if (Utils.getConfigBoolean("furaffinity.login", true)) {
            LOGGER.info("Logging in using cookies");
            String faCookies = Utils.getConfigString("furaffinity.cookies", "a=897bc45b-1f87-49f1-8a85-9412bc103e7a;b=c8807f36-7a85-4caf-80ca-01c2a2368267");
            warnAboutSharedAccount(faCookies);
            cookies = getCookiesFromString(faCookies);
        }
    }

    private void warnAboutSharedAccount(String loginCookies) {
        if (loginCookies.equals("a=897bc45b-1f87-49f1-8a85-9412bc103e7a;b=c8807f36-7a85-4caf-80ca-01c2a2368267")) {
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_ERRORED,
                    "WARNING: Using the shared furaffinity account exposes both your IP and how many items you downloaded to the other users of the share account");
        }
    }

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool furaffinityThreadPool
            = new DownloadThreadPool( "furaffinity");

    @Override
    public DownloadThreadPool getThreadPool() {
        return furaffinityThreadPool;
    }

    public FuraffinityRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "furaffinity.net";
    }

    @Override
    public String getHost() {
        return "furaffinity";
    }
    @Override
    public boolean hasDescriptionSupport() {
        return false;
    }
    @Override
    public Document getFirstPage() throws IOException {
        setCookies();
        return Http.url(url).cookies(cookies).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Elements nextPageUrl = doc.select("a.right");
        if (nextPageUrl.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = urlBase + nextPageUrl.first().attr("href");

        sleep(500);
        Document nextPage = Http.url(nextUrl).cookies(cookies).get();

        return nextPage;
    }

    private String getImageFromPost(String url) {
        sleep(1000);
        Document d = null;
        try {
            d = Http.url(url).cookies(cookies).get();
            Elements links = d.getElementsByTag("a");
            for (Element link : links) {
                if (link.text().equals("Download")) {
                    LOGGER.info("Found image " + link.attr("href"));
                   return "https:" + link.attr("href");
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> urls = new ArrayList<>();
        Elements urlElements = page.select("figure.t-image > b > u > a");
        for (Element e : urlElements) {
            String urlToAdd = getImageFromPost(urlBase + e.select("a").first().attr("href"));
            if (url != null) {
                if (urlToAdd.startsWith("http")) {
                    urls.add(urlToAdd);
                }
            }
        }
        return urls;
    }
    @Override
    public List<String> getDescriptionsFromPage(Document page) {
        List<String> urls = new ArrayList<>();
        Elements urlElements = page.select("figure.t-image > b > u > a");
        for (Element e : urlElements) {
            urls.add(urlBase + e.select("a").first().attr("href"));
            LOGGER.debug("Desc2 " + urlBase + e.select("a").first().attr("href"));
        }
        return urls;
    }
    @Override
    public int descSleepTime() {
        return 400;
    }
    public String getDescription(String page) {
        try {
            // Fetch the image page
            Response resp = Http.url(page)
                    .referrer(this.url)
                    .response();
            cookies.putAll(resp.cookies());

            // Try to find the description
            Elements els = resp.parse().select("td[class=alt1][width=\"70%\"]");
            if (els.isEmpty()) {
                LOGGER.debug("No description at " + page);
                throw new IOException("No description found");
            }
            LOGGER.debug("Description found!");
            Document documentz = resp.parse();
            Element ele = documentz.select("td[class=alt1][width=\"70%\"]").get(0); // This is where the description is.
            // Would break completely if FurAffinity changed site layout.
            documentz.outputSettings(new Document.OutputSettings().prettyPrint(false));
            ele.select("br").append("\\n");
            ele.select("p").prepend("\\n\\n");
            LOGGER.debug("Returning description at " + page);
            String tempPage = Jsoup.clean(ele.html().replaceAll("\\\\n", System.getProperty("line.separator")), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
            return documentz.select("meta[property=og:title]").attr("content") + "\n" + tempPage; // Overridden saveText takes first line and makes it the file name.
        } catch (IOException ioe) {
            LOGGER.info("Failed to get description " + page + " : '" + ioe.getMessage() + "'");
            return null;
        }
    }
    @Override
    public boolean saveText(URL url, String subdirectory, String text, int index) {
        //TODO Make this better please?
        try {
            stopCheck();
        } catch (IOException e) {
            return false;
        }
        String newText = "";
        String saveAs = "";
        File saveFileAs;
        saveAs = text.split("\n")[0];
        saveAs = saveAs.replaceAll("^(\\S+)\\s+by\\s+(.*)$", "$2_$1");
        for (int i = 1;i < text.split("\n").length; i++) {
            newText = newText.replace("\\","").replace("/","").replace("~","") + "\n" + text.split("\n")[i];
        }
        try {
            if (!subdirectory.equals("")) {
                subdirectory = File.separator + subdirectory;
            }
            saveFileAs = new File(
                    workingDir.getCanonicalPath()
                            + subdirectory
                            + File.separator
                            + saveAs
                            + ".txt");
            // Write the file
            FileOutputStream out = (new FileOutputStream(saveFileAs));
            out.write(text.getBytes());
            out.close();
        } catch (IOException e) {
            LOGGER.error("[!] Error creating save file path for description '" + url + "':", e);
            return false;
        }
        LOGGER.debug("Downloading " + url + "'s description to " + saveFileAs);
        if (!saveFileAs.getParentFile().exists()) {
            LOGGER.info("[+] Creating directory: " + Utils.removeCWD(saveFileAs.getParent()));
            saveFileAs.getParentFile().mkdirs();
        }
        return true;
    }
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern
                .compile("^https?://www\\.furaffinity\\.net/gallery/([-_.0-9a-zA-Z]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("Expected furaffinity.net URL format: "
                + "www.furaffinity.net/gallery/username  - got " + url
                + " instead");
    }


}