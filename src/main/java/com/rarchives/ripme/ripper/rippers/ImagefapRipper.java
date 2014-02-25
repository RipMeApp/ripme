package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractRipper;

public class ImagefapRipper extends AbstractRipper {

	private static final String HOST = "imagefap.com";

	public ImagefapRipper(URL url) throws MalformedURLException {
		super(url);
	}
	
	/**
	 * Reformat given URL into the desired format (all images on single page)
	 */
	public void sanitizeURL() throws MalformedURLException {
	    String gid = null;
	    Pattern p = Pattern.compile("^.*imagefap.com/gallery.php?gid=([0-9]{1,}).*$");
	    Matcher m = p.matcher(this.url.toExternalForm());
	    if (m.matches()) {
	        gid = m.group(1);
	    } else {
	        p = Pattern.compile("^.*imagefap.com/pictures/([0-9]{1,}).*$");
            m = p.matcher(this.url.toExternalForm());
            if (m.matches()) {
                gid = m.group(1);
            }
	    }
	    if (gid == null) {
	        throw new MalformedURLException("Expected imagefap.com gallery formats:"
	                + "imagefap.com/gallery.php?gid=####... or"
	                + "imagefap.com/pictures/####...");
	    }
	    this.url = new URL("http://www.imagefap.com/gallery.php?gid=" + gid + "&view=2");
	}

	public void rip() throws IOException {
        System.err.println("Connecting to " + this.url.toExternalForm());
        Document doc = Jsoup.connect(this.url.toExternalForm()).get();
        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = thumb.attr("src");
            image = image.replaceAll("http://x.*.fap.to/images/thumb/", "http://fap.to/images/full/");
            System.err.println(image);
        }
	}

	public boolean canRip(URL url) {
		if (!url.getHost().endsWith(HOST)) {
			return false;
		}
		return true;
	}

}

