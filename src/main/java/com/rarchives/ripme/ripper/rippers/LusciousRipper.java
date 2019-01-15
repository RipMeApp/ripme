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
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

public class LusciousRipper extends AbstractHTMLRipper {
	private static final int RETRY_COUNT = 5; // Keeping it high for read timeout exception.

	private Pattern p = Pattern.compile("^https?://(?:members.)?luscious\\.net/albums/([-_.0-9a-zA-Z]+).*$");
	private DownloadThreadPool lusciousThreadPool = new DownloadThreadPool("lusciousThreadPool");

	public LusciousRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public String getDomain() {
		return "luscious.net";
	}

	@Override
	public String getHost() {
		return "luscious";
	}

	@Override
	public Document getFirstPage() throws IOException {
		// "url" is an instance field of the superclass
		Document page = Http.url(url).get();
		LOGGER.info("First page is " + url);
		return page;
	}

	@Override
	public List<String> getURLsFromPage(Document page) {
		List<String> urls = new ArrayList<>();
		Elements urlElements = page.select("div.item.thumbnail.ic_container > a");
		for (Element e : urlElements) {
			urls.add(e.attr("abs:href"));
		}

		return urls;
	}

	@Override
	public Document getNextPage(Document doc) throws IOException {
		// luscious sends xhr requests to nextPageUrl and appends new set of images to the current page while in browser.
		// Simply GET the nextPageUrl also works. Therefore, we do this...
		Element nextPageElement = doc.select("div#next_page > div > a").first();
		if (nextPageElement == null) {
			throw new IOException("No next page found.");
		}

		return Http.url(nextPageElement.attr("abs:href")).get();
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		Matcher m = p.matcher(url.toExternalForm());
		if (m.matches()) {
			return m.group(1);
		}
		throw new MalformedURLException("Expected luscious.net URL format: "
				+ "luscious.net/albums/albumname \n members.luscious.net/albums/albumname  - got " + url + " instead.");
	}

	@Override
	public void downloadURL(URL url, int index) {
		lusciousThreadPool.addThread(new LusciousDownloadThread(url, index));
	}

	@Override
	public DownloadThreadPool getThreadPool() {
		return lusciousThreadPool;
	}

	public class LusciousDownloadThread extends Thread {
		private URL url;
		private int index;

		public LusciousDownloadThread(URL url, int index) {
			this.url = url;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				Document page = Http.url(url).retries(RETRY_COUNT).get();

				String downloadUrl = page.select(".icon-download").attr("abs:href");
				if (downloadUrl.equals("")) {
					// This is here for pages with mp4s instead of images.
					downloadUrl = page.select("div > video > source").attr("src");
					if (!downloadUrl.equals("")) {
						throw new IOException("Could not find download url for image or video.");
					}
				}

				//If a valid download url was found.
				addURLToDownload(new URL(downloadUrl), getPrefix(index));

			} catch (IOException e) {
				LOGGER.error("Error downloadiong url " + url, e);
			}
		}

	}
}
