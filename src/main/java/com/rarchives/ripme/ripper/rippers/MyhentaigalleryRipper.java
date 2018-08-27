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

public class MyhentaigalleryRipper extends AbstractHTMLRipper {
    private static boolean isTag;

    public MyhentaigalleryRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "myhentaigallery";
    }

    @Override
    public String getDomain() {
        return "myhentaigallery.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://myhentaigallery.com/gallery/thumbnails/([0-9]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("Expected myhentaicomics.com URL format: " +
                "myhentaicomics.com/index.php/albumName - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

//    @Override
//    public Document getNextPage(Document doc) throws IOException {
//        // Find next page
//        String nextUrl = "";
//        Element elem = doc.select("a.ui-icon-right").first();
//        String nextPage = elem.attr("href");
//        Pattern p = Pattern.compile("/index.php/[a-zA-Z0-9_-]*\\?page=\\d");
//        Matcher m = p.matcher(nextPage);
//        if (m.matches()) {
//            nextUrl = "https://myhentaicomics.com" + m.group(0);
//        }
//        if (nextUrl.equals("")) {
//            throw new IOException("No more pages");
//        }
//        // Sleep for half a sec to avoid getting IP banned
//        sleep(500);
//        return Http.url(nextUrl).get();
//    }



    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select(".comic-thumb > img")) {
            String imageSource = el.attr("src");
            // We replace thumbs with resizes so we can the full sized images
            imageSource = imageSource.replace("thumbnail", "original");
            result.add("https://" + getDomain() + imageSource);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }


}