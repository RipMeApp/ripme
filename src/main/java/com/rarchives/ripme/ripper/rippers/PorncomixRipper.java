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

public class PorncomixRipper extends AbstractHTMLRipper {

    public PorncomixRipper(URL url) throws IOException {
    super(url);
    }

        @Override
        public String getHost() {
            return "porncomix";
        }

        @Override
        public String getDomain() {
            return "porncomix.info";
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("https?://www.porncomix.info/([a-zA-Z0-9_\\-]*)/?$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected proncomix URL format: " +
                            "porncomix.info/comic - got " + url + " instead");
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<>();
                for (Element el : doc.select("div.single-post > div.gallery > dl > dt > a > img")) {
                    String imageSource = el.attr("data-lazy-src");
                    // We remove the .md from images so we download the full size image
                    // not the thumbnail ones
                        imageSource = imageSource.replaceAll("-\\d\\d\\dx\\d\\d\\d", "");
                        result.add(imageSource);
                    }
                return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }
    }
