package com.rarchives.ripme.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper;
import com.rarchives.ripme.ripper.rippers.VidbleRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurAlbum;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurImage;
import com.rarchives.ripme.ripper.rippers.video.GfycatRipper;

public class RipUtils {
    private static final Logger logger = Logger.getLogger(RipUtils.class);

    public static List<URL> getFilesFromURL(URL url) {
        List<URL> result = new ArrayList<URL>();

        logger.debug("Checking " + url);
        // Imgur album
        if ((url.getHost().endsWith("imgur.com")) 
                && url.toExternalForm().contains("imgur.com/a/")) {
            try {
                ImgurAlbum imgurAlbum = ImgurRipper.getImgurAlbum(url);
                for (ImgurImage imgurImage : imgurAlbum.images) {
                    result.add(imgurImage.url);
                }
            } catch (IOException e) {
                logger.error("[!] Exception while loading album " + url, e);
            }
            return result;
        }
        else if (url.getHost().endsWith("gfycat.com")) {
            try {
                String videoURL = GfycatRipper.getVideoURL(url);
                result.add(new URL(videoURL));
            } catch (IOException e) {
                // Do nothing
                logger.warn("Exception while retrieving gfycat page:", e);
            }
            return result;
        }
        else if (url.toExternalForm().contains("vidble.com/album/")) {
            try {
                result.addAll(VidbleRipper.getURLsFromPage(url));
            } catch (IOException e) {
                // Do nothing
                logger.warn("Exception while retrieving vidble page:", e);
            }
            return result;
        }

        // Direct link to image
        Pattern p = Pattern.compile("(https?://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)\\.(jpg|jpeg|gif|png|mp4)(\\?.*)?)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            try {
                URL singleURL = new URL(m.group(1));
                result.add(singleURL);
                return result;
            } catch (MalformedURLException e) {
                logger.error("[!] Not a valid URL: '" + url + "'", e);
            }
        }
        
        if (url.getHost().equals("imgur.com") || 
                url.getHost().equals("m.imgur.com")){
            try {
                // Fetch the page
                Document doc = Jsoup.connect(url.toExternalForm())
                                    .userAgent(AbstractRipper.USER_AGENT)
                                    .get();
                for (Element el : doc.select("meta")) {
                    if (el.attr("property").equals("og:image")) {
                        result.add(new URL(el.attr("content")));
                        return result;
                    }
                }
            } catch (IOException ex) {
                logger.error("[!] Error", ex);
            }
            
        }
        
        logger.error("[!] Unable to rip URL: " + url);
        return result;
    }
    
    public static Pattern getURLRegex() {
        return Pattern.compile("(https?://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*))");
    }
}
