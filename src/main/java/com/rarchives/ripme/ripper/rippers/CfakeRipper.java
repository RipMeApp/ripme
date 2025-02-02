package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class CfakeRipper extends AbstractHTMLRipper {

    public CfakeRipper(URL url) throws IOException {
    super(url);
    }

        @Override
        public String getHost() {
            return "cfake";
        }

        @Override
        public String getDomain() {
            return "cfake.com";
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("https?://cfake\\.com/picture/([a-zA-Z1-9_-]*)/\\d+/?$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected cfake URL format: " +
                            "cfake.com/picture/MODEL/ID - got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            return Http.url(url).get();
        }

        @Override
        public Document getNextPage(Document doc) throws IOException {
            // We use comic-nav-next to the find the next page
            Element elem = doc.select("td > div.next > a").first();
                if (elem == null) {
                    throw new IOException("No more pages");
                }
                String nextPage = elem.attr("href");
                // Some times this returns a empty string
                // This for stops that
                if (nextPage.equals("")) {
                    return null;
                }
                else {
                    return Http.url("http://cfake.com" + nextPage).get();
                }
            }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<>();
                for (Element el : doc.select("table.display > tbody > tr > td > table > tbody > tr > td > a")) {
                    if (el.attr("href").contains("upload")) {
                        return result;
                    } else {
                        String imageSource = el.select("img").attr("src");
                        // We remove the .md from images so we download the full size image
                        // not the thumbnail ones
                        imageSource = imageSource.replace("thumbs", "photos");
                        result.add("http://cfake.com" + imageSource);
                    }
                }
                return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }
    }
