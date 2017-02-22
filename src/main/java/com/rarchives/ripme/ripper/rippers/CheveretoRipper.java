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

public class CheveretoRipper extends AbstractHTMLRipper {

    public CheveretoRipper(URL url) throws IOException {
    super(url);
    }

    public static List<String> explicit_domains_1 = Arrays.asList("www.ezphotoshare.com", "hushpix.com");
        @Override
        public String getHost() {
            String host = url.toExternalForm().split("/")[2];
            return host;
        }

        @Override
        public String getDomain() {
            String host = url.toExternalForm().split("/")[2];
            return host;
        }

        @Override
        public boolean canRip(URL url) {
            String url_name = url.toExternalForm();
            if (explicit_domains_1.contains(url_name.split("/")[2]) == true) {
                Pattern pa = Pattern.compile("(?:https?://)?(?:www\\.)?[a-z1-9]*\\.[a-z1-9]*/album/([a-zA-Z1-9]*)/?$");
                Matcher ma = pa.matcher(url.toExternalForm());
                if (ma.matches()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("(?:https?://)?(?:www\\.)?[a-z1-9]*\\.[a-z1-9]*/album/([a-zA-Z1-9]*)/?$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected chevereto URL format: " +
                            "site.domain/album/albumName or site.domain/username/albums- got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            return Http.url(url).get();
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<String>();
                for (Element el : doc.select("a.image-container > img")) {
                    String imageSource = el.attr("src");
                    // We remove the .md from images so we download the full size image
                    // not the medium ones
                    imageSource = imageSource.replace(".md", "");
                    result.add(imageSource);
                }
            return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }


    }
