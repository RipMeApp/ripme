package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class HentaiCafeRipper extends AbstractHTMLRipper {

    public HentaiCafeRipper(URL url) throws IOException {
        super(url);
    }

        @Override
        public String getHost() {
            return "hentai";
        }

        @Override
        public String getDomain() {
            return "hentai.cafe";
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("https?://hentai\\.cafe/([a-zA-Z0-9_\\-%]*)/?$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected hentai.cafe URL format: " +
                            "hentai.cafe/COMIC - got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            Document tempDoc = Http.url(url).get();
            return Http.url(tempDoc.select("div.last > p > a.x-btn").attr("href")).get();
        }

        @Override
        public Document getNextPage(Document doc) throws IOException {
            String nextPageURL = doc.select("div[id=page] > div.inner > a").attr("href");
            int totalPages = Integer.parseInt(doc.select("div.panel > div.topbar > div > div.topbar_right > div.tbtitle > div.text").text().replace(" ⤵", ""));
            String[] nextPageURLSplite = nextPageURL.split("/");
            // This checks if the next page number is greater than the total number of pages
            if (totalPages >= Integer.parseInt(nextPageURLSplite[nextPageURLSplite.length -1])) {
                return Http.url(nextPageURL).get();
            }
            throw new IOException("No more pages");
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<>();
                result.add(doc.select("div[id=page] > div.inner > a > img.open").attr("src"));
                return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }
    }
