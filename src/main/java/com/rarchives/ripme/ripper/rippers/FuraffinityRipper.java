package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

public class FuraffinityRipper extends AbstractHTMLRipper {

	static Map<String, String> cookies=null;
	static final String urlBase = "http://www.furaffinity.net";

	// Thread pool for finding direct image links from "image" pages (html)
	private DownloadThreadPool furaffinityThreadPool = new DownloadThreadPool(
			"furaffinity");

	@Override
	public DownloadThreadPool getThreadPool() {
		return furaffinityThreadPool;
	}

	public FuraffinityRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public String getDomain() {
		return "furaffinity.net";
	}

	@Override
	public String getHost() {
		return "furaffinity";
	}

	@Override
	public Document getFirstPage() throws IOException {
		if (cookies == null || cookies.size() == 0) {
			JPasswordField passwordField=new JPasswordField();
			String user = JOptionPane.showInputDialog("Username");
			JOptionPane.showMessageDialog(null,passwordField,"Password",JOptionPane.QUESTION_MESSAGE|JOptionPane.OK_OPTION);
			String pass = Arrays.toString(passwordField.getPassword());

			Response loginPage=Http.url(urlBase+"/login/")
					.referrer(urlBase)
					.response();
			cookies=loginPage.cookies();
			System.out.println("Cookies: "+cookies);
			
			Map<String,String> formData=new HashMap<String,String>();
			formData.put("action", "login");
			formData.put("retard_protection", "1");
			formData.put("name", user);
			formData.put("pass", pass);
			formData.put("login", "Login to FurAffinity");
			
			Response doLogin=Http.url(urlBase+"/login/")
					.referrer(urlBase+"/login/")
					.cookies(cookies)
					.data(formData)
					.method(Method.POST)
					.response();
			cookies.putAll(doLogin.cookies());
			System.out.println("Cookies: "+cookies);
		}

		return Http.url(url).cookies(cookies).get();
	}

	@Override
	public Document getNextPage(Document doc) throws IOException {
		// Find next page
		Elements nextPageUrl = doc.select("td[align=right] form");
		String nextUrl = urlBase+nextPageUrl.first().attr("action");
		if (nextPageUrl.size() == 0) {
			throw new IOException("No more pages");
		}
		sleep(500);
		Document nextPage = Http.url(nextUrl).cookies(cookies).get();

		Elements hrefs = nextPage.select("div#no-images");
		if (hrefs.size() != 0) {
			throw new IOException("No more pages");
		}
		return nextPage;
	}

	@Override
	public List<String> getURLsFromPage(Document page) {
		List<String> urls = new ArrayList<String>();
		Elements urlElements = page.select("b[id^=sid_]");
		for (Element e : urlElements) {
			urls.add(urlBase + e.select("a").first().attr("href"));
		}
		return urls;
	}

	@Override
	public void downloadURL(URL url, int index) {
		furaffinityThreadPool.addThread(new FuraffinityDocumentThread(url,
				index));
		sleep(250);
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		Pattern p = Pattern
				.compile("^https?://www\\.furaffinity\\.net/gallery/([-_.0-9a-zA-Z]+).*$");
		Matcher m = p.matcher(url.toExternalForm());
		if (m.matches()) {
			return m.group(1);
		}
		throw new MalformedURLException("Expected furaffinity.net URL format: "
				+ "www.furaffinity.net/gallery/username  - got " + url
				+ " instead");
	}

	private class FuraffinityDocumentThread extends Thread {
		private URL url;
		private int index;

		public FuraffinityDocumentThread(URL url, int index) {
			super();
			this.url = url;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				Document doc = Http.url(url).cookies(cookies).get();
				// Find image
				Elements donwloadLink = doc.select("div.alt1 b a[href^=//d.facdn.net/]");
				if (donwloadLink.size() == 0) {
					logger.warn("Could not download " + this.url);
					return;
				}
				String link = "http:" + donwloadLink.first().attr("href");
				logger.info("Found URL " + link);
				addURLToDownload(new URL(link),"","",url.toExternalForm(),cookies);
			} catch (IOException e) {
				logger.error("[!] Exception while loading/parsing " + this.url,
						e);
			}
		}
	}

}
