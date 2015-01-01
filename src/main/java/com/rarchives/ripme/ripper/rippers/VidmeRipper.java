package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class VidmeRipper extends AbstractHTMLRipper {

	public VidmeRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public String getDomain() {
		return "vid.me";
	}

	@Override
	public String getHost() {
		return "vid";
	}

	@Override
	public Document getFirstPage() throws IOException {
		return Http.url(url).get();
	}

	@Override
	public List<String> getURLsFromPage(Document page) {
		List<String> result = new LinkedList<>();
		for(Element elem : page.select("a.js-download-video-link")){
			String link = StringEscapeUtils.unescapeHtml(elem.attr("data-href").toString());
			result.add(link);
		}
		return result;
	}

	@Override
	public void downloadURL(URL url, int index) {
		 addURLToDownload(url, getPrefix(index));
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		Pattern p = Pattern.compile("^https?://vid\\.me/([a-zA-Z0-9]+).*$");
		Matcher m = p.matcher(url.toExternalForm());
		if (m.matches()) {
			// Return the text contained between () in the regex
			return m.group(1);
		}
		throw new MalformedURLException("Expected imgur.com URL format: "
				+ "imgur.com/a/albumid - got " + url + " instead");
	}

}
