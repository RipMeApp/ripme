package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

public class HqpornerRipper extends AbstractHTMLRipper {

	private static final Logger logger = LogManager.getLogger(HqpornerRipper.class);

	private static final String VIDEO_URL_PREFIX = "https://hqporner.com";

	private Pattern p1 = Pattern.compile("https?://hqporner.com/hdporn/([a-zA-Z0-9_-]*).html/?$"); // video pattern.
	private Pattern p2 = Pattern.compile("https://hqporner.com/([a-zA-Z0-9/_-]+)"); // category/top/actress/studio pattern.
	private Pattern p3 = Pattern.compile("https?://[A-Za-z0-9/.-_]+\\.mp4"); // to match links ending with .mp4
	private DownloadThreadPool hqpornerThreadPool = new DownloadThreadPool("hqpornerThreadPool");
	private String subdirectory = "";

	public HqpornerRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public String getHost() {
		return "hqporner";
	}

	@Override
	public String getDomain() {
		return "hqporner.com";
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {

		Matcher m1 = p1.matcher(url.toExternalForm());
		if (m1.matches()) {
			return m1.group(1);
		}
		Matcher m2 = p2.matcher(url.toExternalForm());
		if (m2.matches()) {
			if (m2.group(1).indexOf('/') == -1) {
				return m2.group(1);
			}
			return m2.group(1).substring(0, m2.group(1).indexOf('/')); //returns actress/category/top/studio
		}
		throw new MalformedURLException("Expected hqporner URL format: " + "hqporner.com/hdporn/NAME\n"
				+ "hqporner.com/category/myfavcategory\n" + "hqporner.com/actress/myfavactress\n"
				+ "hqporner.com/studio/myFavStudio\n" + " - got " + url + " instead.");
	}

	@Override
	public Document getFirstPage() throws IOException, URISyntaxException {
		return super.getFirstPage();
	}

	@Override
	public List<String> getURLsFromPage(Document doc) {
		List<String> result = new ArrayList<>();
		Matcher m1 = p1.matcher(this.url.toExternalForm()); // video url.
		Matcher m2 = p2.matcher(this.url.toExternalForm()); // category/top/actress/studio url.

		if (m1.matches()) {
			//subdirectory = subdirectory
			result.add(this.url.toExternalForm());
			return result;
		} else if (m2.matches()) {
			if (m2.group(1).indexOf('/') != -1)
				subdirectory = m2.group(1).substring(m2.group(1).indexOf('/') + 1);
			result = getAllVideoUrls(doc);
			return result;
		}
		//empty array for rest.
		return result;
	}

	public List<String> getAllVideoUrls(Document doc) {
		// div.6u h3  a.click-trigger
		List<String> result = new ArrayList<>();
		Elements videoLinks = doc.select("div.6u h3  a.click-trigger");
		for (Element e : videoLinks) {
			if (e.hasAttr("href")) {
				result.add(VIDEO_URL_PREFIX + e.attr("href"));
			}
		}

		return result;
	}

	@Override
	public boolean tryResumeDownload() {
		return true;
	}

	@Override
	public void downloadURL(URL url, int index) {
		hqpornerThreadPool.addThread(new HqpornerDownloadThread(url, index, subdirectory));
	}

	@Override
	public Document getNextPage(Document doc) throws IOException {
		Elements pageNumbers = doc.select("ul.pagination a[href]");
		if (!pageNumbers.isEmpty() && pageNumbers.last().text().contains("Next")) {
			return Http.url(VIDEO_URL_PREFIX + pageNumbers.last().attr("href")).get();
		}
		throw new IOException("No next page found.");
	}

	@Override
	public DownloadThreadPool getThreadPool() {
		return hqpornerThreadPool;
	}

	@Override
	public boolean useByteProgessBar() {
		return true;
	}

	private class HqpornerDownloadThread implements Runnable {

		private URL hqpornerVideoPageUrl;
		//private int index;
		private String subdirectory;

		public HqpornerDownloadThread(URL url, int index, String subdirectory) {
			this.hqpornerVideoPageUrl = url;
			//this.index = index;
			this.subdirectory = subdirectory;
		}

		@Override
		public void run() {
			fetchVideo();
		}

		public void fetchVideo() {
			try {

				Document doc = Http.url(hqpornerVideoPageUrl).retries(3).get();
				String downloadUrl = null;
				String videoPageUrl = "https:" + doc.select("div.videoWrapper > iframe").attr("src");

				if (videoPageUrl.contains("mydaddy")) {
					downloadUrl = getVideoFromMyDaddycc(videoPageUrl);
				} else if (videoPageUrl.contains("flyflv")) {
					downloadUrl = getVideoFromFlyFlv(videoPageUrl);
				} else {
					//trying a generic selector to grab video url.
					downloadUrl = getVideoFromUnknown(videoPageUrl);
				}

				if (downloadUrl != null) {
					addURLToDownload(new URI(downloadUrl).toURL(), "", subdirectory, "", null, getVideoName(), "mp4");
				}

			} catch (IOException | URISyntaxException e) {
				logger.error("[!] Exception while downloading video.", e);
			}
		}

		private String getVideoFromMyDaddycc(String videoPageUrl) {
			Pattern p = Pattern.compile("(//[a-zA-Z0-9\\.]+/pub/cid/[a-z0-9]+/1080.mp4)");
			try {
				logger.info("Downloading from mydaddy " + videoPageUrl);
				Document page = Http.url(videoPageUrl).referrer(hqpornerVideoPageUrl).get();
				Matcher m = p.matcher(page.html());
				logger.info(page.html());
				if (m.find()) {
					return "https:" + m.group(0);
				}

			} catch (IOException e) {
				logger.error("Unable to get page with video");
			}
			return null;
		}

		private String getVideoFromFlyFlv(String videoPageUrl) {
			try {
				logger.info("Downloading from flyflv " + videoPageUrl);
				Document page = Http.url(videoPageUrl).referrer(hqpornerVideoPageUrl).get();
				String[] videoSizes = { "1080p", "720p", "360p" };
				for (String videoSize : videoSizes) {
					String urlToReturn = page.select("video > source[label=" + videoSize).attr("src");
					if (urlToReturn != null && !urlToReturn.equals("")) {
						return "https:" + urlToReturn;
					}
				}

			} catch (IOException e) {
				logger.error("Unable to get page with video");
			}
			return null;
		}

		private String getVideoFromUnknown(String videoPageurl) {
			// If video host is neither daddycc or flyflv TRY generic way.
			// 1. Search any src$=.mp4
			// 2. Pattern match http(s)://.../../abcd.mp4
			// 3. GET all src link with same host and run 2.

			try {
				logger.info("Trying to download from unknown video host " + videoPageurl);
				URL url = new URI(videoPageurl).toURL();
				Response response = Http.url(url).referrer(hqpornerVideoPageUrl).response();
				Document doc = response.parse();

				// 1. Search for src$=.mp4
				Elements endingWithMp4 = doc.select("[src$=.mp4]");
				if (!endingWithMp4.isEmpty()) {
					List<String> list = new ArrayList<>();
					endingWithMp4.forEach((e) -> list.add(e.attr("src")));
					return getBestQualityLink(list);
				}

				// 2. Pattern match https?://somehost.cc/example123/abcd.mp4
				String link = matchUrlByPattern(p3, doc.html());
				if (link != null) {
					return link;
				}

				// 3. GET all src link with same host and run 2.
				link = null;
				Elements allElementsWithSrc = doc.select("[src*=" + url.getHost() + "]"); //all urls from same host.
				allElementsWithSrc = allElementsWithSrc.select("[src~=/[A-Za-z0-9_-]+$]"); // remove links with extensions( .js).
				for (Element e : allElementsWithSrc) {
					Document d = Http.url(e.attr("src")).referrer(url.getHost()).get();
					link = matchUrlByPattern(p3, d.html());
					if (link != null) {
						return link;
					}
				}

			} catch (IOException | URISyntaxException e) {
				logger.error("Unable to get video url using generic methods.");
			}

			// RIP unknown ripper.
			logger.error("Unable to get video url using generic methods.");
			return null;

		}

		private String matchUrlByPattern(Pattern pattern, String html) {
			// Step 2. function
			Matcher m = pattern.matcher(html);
			List<String> list = new ArrayList<>();
			while (m.find()) {
				list.add(m.group());
			}
			if (!list.isEmpty()) {
				return getBestQualityLink(list);
			}

			return null;
		}

		private String getVideoName() {
			try {
				String filename = getGID(hqpornerVideoPageUrl);
				return filename;
			} catch (MalformedURLException e) {
				return "1080";
			}
		}

	}// class HqpornerDownloadThread

	public String getBestQualityLink(List<String> list) {
		// return link with the highest quality subsubstring. Keeping it simple for now.
		// 1080 > 720 > 480 > 360 > 240
		if (list.isEmpty()) {
			return null;
		}

		String[] qualities = { "2160", "2160p", "1440", "1440p", "1080", "1080p", "720", "720p", "480", "480p" };
		for (String quality : qualities) {
			for (String s : list) {
				if (s.contains(quality)) {
					return s;
				}
			}
		}
		// Could not find the best link. Return fist link.
		return list.get(0);
	}

}
