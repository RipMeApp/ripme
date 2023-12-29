package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
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

public class HbrowseRipper extends AbstractHTMLRipper {

    public HbrowseRipper(URL url) throws IOException {
    super(url);
    }

        @Override
        public String getHost() {
            return "hbrowse";
        }

        @Override
        public String getDomain() {
            return "hbrowse.com";
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("https?://www.hbrowse.com/(\\d+)/[a-zA-Z0-9]*");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected hbrowse.com URL format: " +
                            "hbrowse.com/ID/COMICID - got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            Document tempDoc = Http.url(url).get();
            return Http.url("https://www.hbrowse.com" + tempDoc.select("td[id=pageTopHome] > a[title=view thumbnails (top)]").attr("href")).get();
        }

        @Override
        public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
            try {
                Document doc = getCachedFirstPage();
                String title = doc.select("div[id=main] > table.listTable > tbody > tr > td.listLong").first().text();
                return getHost() + "_" + title + "_" + getGID(url);
            } catch (Exception e) {
                // Fall back to default album naming convention
                LOGGER.warn("Failed to get album title from " + url, e);
            }
            return super.getAlbumTitle(url);
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<String>();
            for (Element el : doc.select("table > tbody > tr > td > a > img")) {
                String imageURL = el.attr("src").replace("/zzz", "");
                result.add("https://www.hbrowse.com" + imageURL);
            }
                return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }
    }
