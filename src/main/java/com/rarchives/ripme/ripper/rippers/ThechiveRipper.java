
package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ThechiveRipper extends AbstractHTMLRipper {

    public ThechiveRipper(URL url) throws IOException {
    super(url);
    }

    @Override
    public String getHost() {
        return "thechive";
    }

    @Override
    public String getDomain() {
        return "thechive.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://thechive.com/[0-9]*/[0-9]*/[0-9]*/([a-zA-Z0-9_\\-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            boolean isTag = false;
            return m.group(1);
        }
        throw new MalformedURLException("Expected thechive.com URL format: " +
                        "thechive.com/YEAR/MONTH/DAY/POSTTITLE/ - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("img.attachment-gallery-item-full")) {
            String imageSource = el.attr("src");
            // We replace thumbs with resizes so we can the full sized images
            imageSource = imageSource.replace("thumbs", "resizes");
            result.add(imageSource);
                }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }


}
