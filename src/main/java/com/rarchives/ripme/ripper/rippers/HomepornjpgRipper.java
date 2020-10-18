//http://www.homepornjpg.com/creampied-pussy-from-texas-usa.shtml
package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomepornjpgRipper extends AbstractHTMLRipper {

	private static final String DOMAIN = "homepornjpg.com", HOST = "homepornjpg";

	public HomepornjpgRipper(URL url) throws IOException {
		super(url);

	}

	@Override
	public String getHost() {
		return HOST;
	}

	@Override
	public String getDomain() {
		return DOMAIN;
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		Pattern p = Pattern.compile("^http?://www.homepornjpg.com/([a-zA-Z0-9]+).*$");
		Matcher m = p.matcher(url.toExternalForm());
		if (m.matches()) {
            // Return the text contained between () in the regex
			String urlInString = url.toExternalForm()
					.replace("http://www.homepornjpg.com/", "")
					.replace("https://www.homepornjpg.com/", "")
					.replace(".shtml", "");
            return urlInString;
        }
        throw new MalformedURLException("Expected homepornjpg.com URL format: " +
                        "www.homepornjpg.com - got " + url + " instead");
	}

	@Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {

    	List<String> result = new ArrayList<String>();
    	for (Element el : doc.select("img")) {
            result.add(el.attr("src"));
        }
        return result;

    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}	
