package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class JagodibujaRipper extends AbstractHTMLRipper {

    public JagodibujaRipper(URL url) throws IOException {
    super(url);
    }
        @Override
        public String getHost() {
            return "jagodibuja";
        }

        @Override
        public String getDomain() {
            return "jagodibuja.com";
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("^https?://www.jagodibuja.com/webcomic-living-with-hipstergirl-and-gamergirl-english/([a-zA-Z0-9_\\-]*)/?");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return "living-with-hipstergirl-and-gamergirl";
            }
            throw new MalformedURLException("Expected jagodibuja.com gallery formats hwww.jagodibuja.com/webcomic-living-with-hipstergirl-and-gamergirl-english/COMIC/ got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            return Http.url(url).get();
        }

        @Override
        public Document getNextPage(Document doc) throws IOException {
            // Find next page
            String nextPage = "";
            Element elem = null;
            elem = doc.select("div.entry-attachment > div.attachment > a").first();
                if (elem == null) {
                    throw new IOException("No more pages");
                }
                nextPage = elem.attr("href");
                if (nextPage == "") {
                    throw new IOException("No more pages");
                }
                else {
                    return Http.url(nextPage).get();
                }
            }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<String>();
                Element elem = doc.select("span.full-size-link > a").first();
                result.add(elem.attr("href"));
            return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            sleep(500);
            addURLToDownload(url, getPrefix(index));
        }


    }
