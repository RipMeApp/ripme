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

import com.rarchives.ripme.utils.Utils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;

public class FuraffinityRipper extends AbstractHTMLRipper {

    static Map<String, String> cookies=null;
    static final String urlBase = "https://www.furaffinity.net";

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
        return true;
    }
    @Override
    public Document getFirstPage() throws IOException {
        if (cookies == null || cookies.size() == 0) {
            login();
        }

        return Http.url(url).cookies(cookies).get();
    }

    private void login() throws IOException {
        String user = new String(Base64.decode("cmlwbWU="));
        String pass = new String(Base64.decode("cmlwbWVwYXNzd29yZA=="));

        Response loginPage = Http.url(urlBase + "/login/")
                                 .referrer(urlBase)
                                 .response();
        cookies = loginPage.cookies();

        Map<String,String> formData = new HashMap<String,String>();
        formData.put("action", "login");
        formData.put("retard_protection", "1");
        formData.put("name", user);
        formData.put("pass", pass);
        formData.put("login", "Login toÂ FurAffinity");

        Response doLogin = Http.url(urlBase + "/login/?ref=" + url)
                               .referrer(urlBase + "/login/")
                               .cookies(cookies)
                               .data(formData)
                               .method(Method.POST)
                               .response();
        cookies.putAll(doLogin.cookies());
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Elements nextPageUrl = doc.select("td[align=right] form");
        if (nextPageUrl.size() == 0) {
            throw new IOException("No more pages");
        }
        String nextUrl = urlBase + nextPageUrl.first().attr("action");

        sleep(500);
        Document nextPage = Http.url(nextUrl).cookies(cookies).get();

        Elements hrefs = nextPage.select("div#no-images");
        if (hrefs.size() != 0) {
            throw new IOException("No more pages");
        }
        return nextPage;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> urls = new ArrayList<String>();
        Elements urlElements = page.select("figure.t-image > b > u > a");
        for (Element e : urlElements) {
            urls.add(urlBase + e.select("a").first().attr("href"));
        }
        return urls;
    }
    @Override
    public List<String> getDescriptionsFromPage(Document page) {
        List<String> urls = new ArrayList<String>();
        Elements urlElements = page.select("figure.t-image > b > u > a");
        for (Element e : urlElements) {
            urls.add(urlBase + e.select("a").first().attr("href"));
            logger.debug("Desc2 " + urlBase + e.select("a").first().attr("href"));
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
                    .cookies(cookies)
                    .response();
            cookies.putAll(resp.cookies());

            // Try to find the description
            Elements els = resp.parse().select("td[class=alt1][width=\"70%\"]");
            if (els.size() == 0) {
                logger.debug("No description at " + page);
                throw new IOException("No description found");
            }
            logger.debug("Description found!");
            Document documentz = resp.parse();
            Element ele = documentz.select("td[class=alt1][width=\"70%\"]").get(0); // This is where the description is.
            // Would break completely if FurAffinity changed site layout.
            documentz.outputSettings(new Document.OutputSettings().prettyPrint(false));
            ele.select("br").append("\\n");
            ele.select("p").prepend("\\n\\n");
            logger.debug("Returning description at " + page);
            String tempPage = Jsoup.clean(ele.html().replaceAll("\\\\n", System.getProperty("line.separator")), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
            String title = documentz.select("meta[property=og:title]").attr("content");
            String tempText = title;
            return tempText + "\n" + tempPage; // Overridden saveText takes first line and makes it the file name.
        } catch (IOException ioe) {
            logger.info("Failed to get description " + page + " : '" + ioe.getMessage() + "'");
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
            int o = url.toString().lastIndexOf('/')-1;
            String test = url.toString().substring(url.toString().lastIndexOf('/',o)+1);
            test = test.replace("/",""); // This is probably not the best way to do this.
            test = test.replace("\\",""); // CLOSE ENOUGH!
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
            logger.error("[!] Error creating save file path for description '" + url + "':", e);
            return false;
        }
        logger.debug("Downloading " + url + "'s description to " + saveFileAs);
        if (!saveFileAs.getParentFile().exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(saveFileAs.getParent()));
            saveFileAs.getParentFile().mkdirs();
        }
        return true;
    }
    @Override
    public void downloadURL(URL url, int index) {
        furaffinityThreadPool.addThread(new FuraffinityDocumentThread(url));
        sleep(250);
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

    private class FuraffinityDocumentThread extends Thread {
        private URL url;

        public FuraffinityDocumentThread(URL url) {
            super();
            this.url = url;
        }

        @Override
        public void run() {
            try {
                Document doc = Http.url(url).cookies(cookies).get();
                // Find image
                Elements donwloadLink = doc.select("div.alt1 b a[href^=//d.facdn.net/]");
                if (donwloadLink.size() == 0) {
                    logger.warn("Could not download " + this.url);
                    return;
                }
                String link = "http:" + donwloadLink.first().attr("href");
                logger.info("Found URL " + link);
                String[] fileNameSplit = link.split("/");
                String fileName = fileNameSplit[fileNameSplit.length -1];
                fileName = fileName.replaceAll("[0-9]*\\.", "");
                String[] fileExtSplit = link.split("\\.");
                String fileExt = fileExtSplit[fileExtSplit.length -1];
                fileName = fileName.replaceAll(fileExt, "");
                File saveAS;
                fileName = fileName.replace("[0-9]*\\.", "");
                saveAS = new File(
                    workingDir.getCanonicalPath()
                            + File.separator
                            + fileName
                            + "."
                            + fileExt);
                addURLToDownload(new URL(link),saveAS,"",cookies);
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }

}
