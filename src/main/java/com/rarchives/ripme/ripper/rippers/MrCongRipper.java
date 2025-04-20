package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class MrCongRipper extends AbstractHTMLRipper {
    private Document currDoc;
    private int lastPageNum;
    private int currPageNum;
    private boolean tagPage = false;

    public MrCongRipper(URL url) throws IOException {
        super(url);
        currPageNum = 1;
    }

    @Override
    public String getHost() {
        return "misskon";
    }

    @Override
    public String getDomain() {
        return "misskon.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        System.out.println(url.toExternalForm());
        Pattern p = Pattern.compile(
                "^https?://misskon\\.com/(\\S*)[0-9]+[-0-9a-zA-Z](-[0-9]+-(?:photos?|ahn)?(-[0-9]+-videos?)?(|/|/[0-9]+)$");
        Pattern p2 = Pattern.compile("^https?://misskon\\.com/tag/(\\S*)/$"); // Added 6-10-21
        Matcher m = p.matcher(url.toExternalForm());
        Matcher m2 = p2.matcher(url.toExternalForm()); // 6-10-21
        if (m.matches()) {
            return m.group(1);
        } else if (m2.matches()) { // Added 6-10-21
            tagPage = true;
            System.out.println("tagPage = TRUE");
            return m2.group(1);
        }

        throw new MalformedURLException("Expected misskon.com URL format: "
                + "misskon.com/GALLERY_NAME(-anh OR -anh/ OR -anh/PAGE_NUMBER OR -anh/PAGE_NUMBER/) - got " + url
                + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException { // returns the root gallery page regardless of actual page
                                                        // number
        // "url" is an instance field of the superclass
        String rootUrlStr;
        URL rootUrl;

        if (!tagPage) {
            rootUrlStr = url.toExternalForm().replaceAll("(|/|/[0-9]+/?)$", "/");
        } else { // 6-10-21
            rootUrlStr = url.toExternalForm().replaceAll("(page/[0-9]+/)$", "page/1/");
        }

        rootUrl = URI.create(rootUrlStr).toURL();
        url = rootUrl;
        currPageNum = 1;
        currDoc = Http.url(url).get();
        getMaxPageNumber(currDoc);
        return currDoc;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        int pageNum = currPageNum;
        String urlStr;
        if (!tagPage) {
            if (pageNum == 1 && lastPageNum > 1) {
                urlStr = url.toExternalForm().concat((pageNum + 1) + "");
                System.out.printf("Old Str: %s   New Str: %s\n", url.toExternalForm(), urlStr);
            } else if (pageNum < lastPageNum) {
                urlStr = url.toExternalForm().replaceAll("(/([0-9]*)/?)$", ("/" + (pageNum + 1) + "/"));
                System.out.printf("Old Str: %s   New Str: %s\n", url.toString(), urlStr);
            } else {
                // System.out.printf("Error: Page number provided goes past last valid page
                // number\n");
                throw (new IOException("Error: Page number provided goes past last valid page number\n"));
            }
        } else { // 6-10-21
            // if (pageNum == 1 && lastPageNum >= 1) {
            if (pageNum == 1 && lastPageNum > 1) { // 6-10-21
                urlStr = url.toExternalForm().concat("page/" + (pageNum + 1) + "");
                System.out.printf("Old Str: %s   New Str: %s\n", url.toExternalForm(), urlStr);
            } else if (pageNum < lastPageNum) {
                urlStr = url.toExternalForm().replaceAll("(page/([0-9]*)/?)$", ("page/" + (pageNum + 1) + "/"));
                System.out.printf("Old Str: %s   New Str: %s\n", url.toString(), urlStr);
            } else {
                // System.out.printf("Error: Page number provided goes past last valid page
                // number\n");
                System.out.print("Error: There is no next page!\n");
                return null;
                // throw (new IOException("Error: Page number provided goes past last valid page
                // number\n"));
            }
        }

        url = URI.create(urlStr).toURL();
        currDoc = Http.url(url).get();
        currPageNum++;// hi
        return currDoc;
    }

    private int getMaxPageNumber(Document doc) {
        if (!tagPage) {
            try {
                // gets the last possible page for the gallery
                lastPageNum = Integer.parseInt(doc.select("div.page-link > a").last().text());
            } catch (Exception e) {
                return 1;
            }
        } else {
            try {
                // gets the last possible page for the gallery
                lastPageNum = Integer.parseInt(doc.select("div.pagination > a").last().text());
                System.out.println("The last page found for " + url + " was " + lastPageNum);
            } catch (Exception e) {
                return 1;
            }
        }

        return lastPageNum;
    }

    private int getCurrentPageNum(Document doc) {
        int currPage; // 6-10-21

        if (!tagPage) {
            currPage = Integer.parseInt(doc.select("div.page-link > span").first().text());
        } else {
            currPage = Integer.parseInt(doc.select("div.pagination > span").first().text());
        }

        System.out.println("The current page was found to be: " + currPage);

        return currPage;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) { // gets the urls of the images
        List<String> result = new ArrayList<>();

        if (!tagPage) {
            for (Element el : doc.select("p > img")) {
                String imageSource = el.attr("data-src");
                result.add(imageSource);
            }

            System.out.println("\n1.)Printing List: " + result + "\n");
        } else {
            for (Element el : doc.select("h2 > a")) {
                String pageSource = el.attr("href");
                if (!pageSource.equals("https://misskon.com/")) {
                    result.add(pageSource);
                    System.out.println("\n" + pageSource + " has been added to the list.");
                }
            }

            System.out.println("\n2.)Printing List: " + result + "\n");
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        if (!tagPage) {
            addURLToDownload(url, getPrefix(index));
        } else {
            try {
                List<String> ls = this.getURLsFromPage(this.currDoc);
                Document np = this.getNextPage(this.currDoc);

                // Creates a list of all sets to download
                while (np != null) {
                    ls.addAll(this.getURLsFromPage(np));
                    np = this.getNextPage(np);
                }

                for (String urlStr : ls) {
                    MrCongRipper mcr = new MrCongRipper(URI.create(urlStr).toURL());
                    mcr.setup();
                    mcr.rip();
                }

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
