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

public class ViewcomicRipper extends AbstractHTMLRipper {

    public ViewcomicRipper(URL url) throws IOException {
        super(url);
    }

        @Override
        public String getHost() {
            return "view-comic";
        }

        @Override
        public String getDomain() {
            return "view-comic.com";
        }

        @Override
        public String getAlbumTitle(URL url) throws MalformedURLException {
            try {
                // Attempt to use album title as GID
                String titleText = getCachedFirstPage().select("title").first().text();
                String title = titleText.replace("Viewcomic reading comics online for free", "");
                title = title.replace("_", "");
                title = title.replace("|", "");
                title = title.replace("…", "");
                title = title.replace(".", "");
                return getHost() + "_" + title.trim();
            } catch (IOException e) {
                // Fall back to default album naming convention
                LOGGER.info("Unable to find title at " + url);
            }
            return super.getAlbumTitle(url);
        }


        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("https?://view-comic.com/([a-zA-Z1-9_-]*)/?$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected view-comic URL format: " +
                            "view-comic.com/COMIC_NAME - got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            return Http.url(url).get();
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<String>();
                for (Element el : doc.select("div.separator > a > img")) {
                    result.add(el.attr("src"));
                }
            return result;
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }
    }
