package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

/**
 *
 * @author losipher
 */
public class EromeRipper extends AbstractHTMLRipper {

    boolean rippingProfile;


    public EromeRipper (URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
            return "erome.com";
    }

    @Override
    public String getHost() {
            return "erome";
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern pa = Pattern.compile("https?://www.erome.com/([a-zA-Z0-9_\\-?=]*)/?");
        Matcher ma = pa.matcher(url.toExternalForm());
        return ma.matches();
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select("div#albums > div.album > a")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
            try {
                // Attempt to use album title as GID
                Element titleElement = getFirstPage().select("meta[property=og:title]").first();
                String title = titleElement.attr("content");
                title = title.substring(title.lastIndexOf('/') + 1);
                return getHost() + "_" + getGID(url) + "_" + title.trim();
            } catch (IOException e) {
                // Fall back to default album naming convention
                LOGGER.info("Unable to find title at " + url);
            } catch (NullPointerException e) {
                return getHost() + "_" + getGID(url);
            }
            return super.getAlbumTitle(url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL(url.toExternalForm().replaceAll("https?://erome.com", "https://www.erome.com"));
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> URLs = new ArrayList<>();
        return getMediaFromPage(doc);
    }

    @Override
    public Document getFirstPage() throws IOException {
        Response resp = Http.url(this.url)
                            .ignoreContentType()
                            .response();

        return resp.parse();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://www.erome.com/[ai]/([a-zA-Z0-9]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.erome.com/([a-zA-Z0-9_\\-?=]+)/?$");
        m = p.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("erome album not found in " + url + ", expected https://www.erome.com/album");
    }

    private List<String> getMediaFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        for (Element el : doc.select("img.img-front")) {
			if (el.hasAttr("src")) {
				if (el.attr("src").startsWith("https:")) {
					results.add(el.attr("src"));
				} else {
					results.add("https:" + el.attr("src"));
				}
			} else if (el.hasAttr("data-src")) {
				//to add images that are not loaded( as all images are lasyloaded as we scroll).
				results.add(el.attr("data-src"));
			}

		}
        for (Element el : doc.select("source[label=HD]")) {
            if (el.attr("src").startsWith("https:")) {
                results.add(el.attr("src"));
            }
            else {
                results.add("https:" + el.attr("src"));
            }
        }
        for (Element el : doc.select("source[label=SD]")) {
            if (el.attr("src").startsWith("https:")) {
                results.add(el.attr("src"));
            }
            else {
                results.add("https:" + el.attr("src"));
            }
        }
        return results;
    }

}
