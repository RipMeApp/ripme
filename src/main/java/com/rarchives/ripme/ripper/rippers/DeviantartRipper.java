package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author MrPlaygon
 * 
 *         NOT using Deviantart API like the old JSON ripper because it is SLOW
 *         and somehow annoying to use. Things to consider: Using the API might
 *         be less work/maintenance later because APIs do not change as
 *         frequently as HTML source code...?
 * 
 * 
 * 
 *         Tested for:
 * 
 *         SFW:
 * 
 *         https://www.deviantart.com/apofiss/gallery/41388863/sceneries
 *         https://www.deviantart.com/kageuri/gallery/
 *         https://www.deviantart.com/kageuri/gallery/?catpath=/
 *         https://www.deviantart.com/apofiss/favourites/39881418/gifts-and
 *         https://www.deviantart.com/kageuri/favourites/
 *         https://www.deviantart.com/kageuri/favourites/?catpath=/
 * 
 *         NSFW:
 * 
 *         https://www.deviantart.com/revpeng/gallery/67734353/Siren-Lee-Agent-of-S-I-R-E-N-S
 * 
 * 
 * 
 *         Login Data (PLEASE DONT ACTUALLY USE!!!):
 * 
 *         email: 5g5_8l4dii5lbbpc@byom.de
 * 
 *         username: 5g58l4dii5lbbpc
 * 
 *         password: 5g5_8l4dii5lbbpc
 * 
 * 
 *
 */
public class DeviantartRipper extends AbstractHTMLRipper {

	private final String username = "5g58l4dii5lbbpc";
	private final String password = "5g5_8l4dii5lbbpc";
	private int offset = 0;
	private boolean usingCatPath = false;
	private int downloadCount = 0;
	private Map<String, String> cookies;
	private DownloadThreadPool deviantartThreadPool = new DownloadThreadPool("deviantart");
	private ArrayList<String> names = new ArrayList<String>();

	// Constants
	private final String referer = "https://www.deviantart.com/";
	private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";

	@Override
	public DownloadThreadPool getThreadPool() {
		return deviantartThreadPool;
	}

	public DeviantartRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	protected String getDomain() {
		return "deviantart.com";
	}

	@Override
	public String getHost() {
		return "deviantart";
	}

	@Override
	protected Document getFirstPage() throws IOException {
		login();
		return Http.url(urlWithParams(this.offset)).cookies(getDACookie()).referrer(referer).userAgent(userAgent).get();
	}

	/**
	 * Stores logged in Cookies. Needed for art pieces only visible to logged in
	 * users.
	 * 
	 * 
	 * @throws IOException when failed to load webpage or failed to read/write
	 *                     cookies in file (used when running multiple instances of
	 *                     RipMe)
	 */
	private void login() throws IOException {

		File f = new File("DACookie.toDelete");
		if (!f.exists()) {
			f.createNewFile();
			f.deleteOnExit();

			// Load login page
			Response res = Http.url("https://www.deviantart.com/users/login").connection().method(Method.GET)
					.referrer(referer).userAgent(userAgent).execute();

			// Find tokens
			Document doc = res.parse();
			Element form = doc.getElementById("login");
			String token = form.select("input[name=\"validate_token\"]").first().attr("value");
			String key = form.select("input[name=\"validate_key\"]").first().attr("value");
			System.out.println(
					"------------------------------" + token + "   &   " + key + "------------------------------");

			// Build Login Data
			HashMap<String, String> loginData = new HashMap<String, String>();
			loginData.put("challenge", "");
			loginData.put("username", username);
			loginData.put("password", password);
			loginData.put("remember_me", "1");
			loginData.put("validate_token", token);
			loginData.put("validate_key", key);
			Map<String, String> cookies = res.cookies();

			// Log in using data. Handle redirect
			res = Http.url("https://www.deviantart.com/users/login").connection().referrer(referer).userAgent(userAgent)
					.method(Method.POST).data(loginData).cookies(cookies).followRedirects(false).execute();
			this.cookies = res.cookies();

			res = Http.url(res.header("location")).connection().referrer(referer).userAgent(userAgent)
					.method(Method.GET).cookies(cookies).followRedirects(false).execute();

			// Store cookies
			updateCookie(res.cookies());

			// Apply agegate
			this.cookies.put("agegate_state", "1");

			// Write Cookie to file for other RipMe Instances
			try {
				FileOutputStream fileOut = new FileOutputStream(f);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(this.cookies);
				out.close();
				fileOut.close();
			} catch (IOException i) {
				i.printStackTrace();
			}

		} else {

			// When cookie file already exists (from another RipMe instance)
			while (this.cookies == null) {
				try {
					Thread.sleep(2000);
					FileInputStream fileIn = new FileInputStream(f);
					ObjectInputStream in = new ObjectInputStream(fileIn);
					this.cookies = (Map<String, String>) in.readObject();
					in.close();
					fileIn.close();
				} catch (IOException | ClassNotFoundException | InterruptedException i) {
					i.printStackTrace();
				}
			}
		}

		System.out.println("------------------------------" + this.cookies + "------------------------------");
	}

	/**
	 * Returns next page Document using offset.
	 */
	@Override
	public Document getNextPage(Document doc) throws IOException {
		this.offset += 24;
		Response re = Http.url(urlWithParams(this.offset)).cookies(getDACookie()).referrer(referer).userAgent(userAgent)
				.response();
		updateCookie(re.cookies());
		Document docu = re.parse();
		Elements messages = docu.getElementsByClass("message");
		System.out.println("------------------------------Current Offset: " + this.offset
				+ " - More Pages?------------------------------");

		if (messages.size() > 0) {

			// if message exists -> last page
			System.out.println("------------------------------Messages amount: " + messages.size()
					+ " - Next Page does not exists------------------------------");
			throw new IOException("No more pages");
		}

		return Http.url(urlWithParams(this.offset)).referrer(referer).userAgent(userAgent).cookies(getDACookie()).get();

	}

	/**
	 * Returns list of Links to the Image pages. NOT links to fullsize image!!! e.g.
	 * https://www.deviantart.com/kageuri/art/RUBY-568396655
	 */
	@Override
	protected List<String> getURLsFromPage(Document page) {

		List<String> result = new ArrayList<String>();

		Element div;
		if (usingCatPath) {
			div = page.getElementById("gmi-");

		} else {
			div = page.getElementsByClass("folderview-art").first().child(0);

		}
		Elements links = div.select("a.torpedo-thumb-link");

		for (Element el : links) {
			result.add(el.attr("href"));

		}

		System.out.println("------------------------------Amount of Images on Page: " + result.size()
				+ "------------------------------");
		System.out.println("------------------------------" + page.location() + "------------------------------");

		return result;
	}

	/**
	 * Starts new Thread to find download link + filename + filetype
	 */
	@Override
	protected void downloadURL(URL url, int index) {
		this.downloadCount += 1;
		System.out.println("------------------------------Download URL Number " + this.downloadCount
				+ "------------------------------");
		System.out.println(
				"------------------------------DAURL: " + url.toExternalForm() + "------------------------------");
		try {
			Response re = Http.url(urlWithParams(this.offset)).cookies(getDACookie()).referrer(referer)
					.userAgent(userAgent).response();
			updateCookie(re.cookies());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Start Thread and add to pool.
		DeviantartImageThread t = new DeviantartImageThread(url);
		deviantartThreadPool.addThread(t);

	}

	@Override
	public String normalizeUrl(String url) {
		return (urlWithParams(this.offset).toExternalForm());
	}

	/**
	 * Returns name of album. Album name consists of 3 words: - Artist (owner of
	 * gallery) - Type (gallery or favorites folder) - Name of the folder
	 * 
	 * Returns artist_type_name
	 */
	@Override
	public String getGID(URL url) throws MalformedURLException {

		String s = url.toExternalForm();
		String artist = "unknown";
		String what = "unknown";
		String albumname = "unknown";

		if (url.toExternalForm().contains("catpath=/")) {
			this.usingCatPath = true;
		}

		Pattern p = Pattern.compile("^https?://www.deviantart\\.com/([a-zA-Z0-9]+).*$");
		Matcher m = p.matcher(s);

		// Artist
		if (m.matches()) {
			artist = m.group(1);
		} else {
			throw new MalformedURLException("Expected deviantart.com URL format: "
					+ "www.deviantart.com/<ARTIST>/gallery/<NUMBERS>/<NAME>\nOR\nwww.deviantart.com/<ARTIST>/favourites/<NUMBERS>/<NAME>\\nOr simply the gallery or favorites of some artist - got "
					+ url + " instead");
		}

		// What is it
		if (s.contains("/gallery/")) {
			what = "gallery";
		} else if (s.contains("/favourites/")) {
			what = "favourites";
		} else {
			throw new MalformedURLException("Expected deviantart.com URL format: "
					+ "www.deviantart.com/<ARTIST>/gallery/<NUMBERS>/<NAME>\nOR\nwww.deviantart.com/<ARTIST>/favourites/<NUMBERS>/<NAME>\nOr simply the gallery or favorites of some artist - got "
					+ url + " instead");
		}

		// Album Name
		Pattern artistP = Pattern
				.compile("^https?://www.deviantart\\.com/[a-zA-Z0-9]+/[a-zA-Z]+/[0-9]+/([a-zA-Z0-9-]+).*$");
		Matcher artistM = artistP.matcher(s);
		if (s.endsWith("?catpath=/")) {
			albumname = "all";
		} else if (s.endsWith("/favourites/") || s.endsWith("/gallery/")) {
			albumname = "featured";
		} else if (artistM.matches()) {
			albumname = artistM.group(1);
		}
		System.out.println("------------------------------Album Name: " + artist + "_" + what + "_" + albumname
				+ "------------------------------");

		return artist + "_" + what + "_" + albumname;

	}

	/**
	 * 
	 * @return Clean URL as String
	 */
	private String cleanURL() {
		return (this.url.toExternalForm().split("\\?"))[0];
	}

	/**
	 * Return correct url with params (catpath) and current offset
	 * 
	 * @return URL to page with offset
	 */
	private URL urlWithParams(int offset) {
		try {
			String url = cleanURL();
			if (this.usingCatPath) {
				return (new URL(url + "?catpath=/&offset=" + offset));
			} else {
				return (new URL(url + "?offset=" + offset));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns Hashmap usable as Cookie for NSFW Artworks Not really needed but
	 * maybe useful later.
	 * 
	 * @return Cookie Hashmap
	 */
	private Map<String, String> getDACookie() {
		return this.cookies;
	}

	private void updateCookie(Map<String, String> m) {

		System.out.println("------------------------------Updating Cookies------------------------------");
		System.out.println(
				"------------------------------Old Cookies: " + this.cookies + " ------------------------------");
		System.out.println("------------------------------New Cookies: " + m + " ------------------------------");
		this.cookies.putAll(m);
		this.cookies.put("agegate_state", "1");
		System.out.println(
				"------------------------------Merged Cookies: " + this.cookies + " ------------------------------");

	}

	/**
	 * Analyzes an image page like
	 * https://www.deviantart.com/kageuri/art/RUBY-568396655 .
	 * 
	 * Looks for download button, follows the authentications and redirects and adds
	 * the Image URL to the download queue. If no download button is present it will
	 * use the largest version of the image.
	 * 
	 * Should work with all filetypes on Deviantart. Tested with .JPG .PNG and .PDF
	 * 
	 * @author MrPlaygon
	 *
	 */
	private class DeviantartImageThread extends Thread {
		private URL url;

		public DeviantartImageThread(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			getFullSizeURL();
		}

		/**
		 * Get URL to Artwork and return fullsize URL with file ending.
		 * 
		 * @param page Like
		 *             https://www.deviantart.com/apofiss/art/warmest-of-the-days-455668450
		 * @return URL like
		 *         https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/intermediary/f/07f7a6bb-2d35-4630-93fc-be249af22b3e/d7jak0y-d20e5932-df72-4d13-b002-5e122037b373.jpg
		 * 
		 * 
		 */
		private void getFullSizeURL() {

			System.out.println("------------------------------------------------------------");
			System.out.println("------------------------------Searching max. Resolution for " + url
					+ "------------------------------");
			sendUpdate(STATUS.LOADING_RESOURCE, "Searching max. resolution for " + url);
			try {
				Response re = Http.url(url).connection().referrer(referer).userAgent(userAgent).cookies(getDACookie())
						.execute();
				Document doc = re.parse();

				// Artwork Title
				String title = doc.select("a.title").first().html();
				title = title.replaceAll("[^a-zA-Z0-9\\.\\-]", "_").toLowerCase();

				int counter = 1;
				if (names.contains(title)) {
					while (names.contains(title + "_" + counter)) {
						counter++;
					}
					title = title + "_" + counter;
				}
				names.add(title);

				// Check for download button
				Element downloadButton = null;

				downloadButton = doc.select("a.dev-page-download").first();

				// Download Button
				if (downloadButton != null) {
					System.out.println("------------------------------Download Button found: "
							+ downloadButton.attr("href") + "------------------------------");

					Response download = Http.url(downloadButton.attr("href")).connection().cookies(getDACookie())
							.method(Method.GET).referrer(referer).userAgent(userAgent).ignoreContentType(true)
							.followRedirects(true).execute();
					URL location = download.url();

					String[] filetypePart = download.header("Content-Disposition").split("\\.");

					System.out.println("------------------------------Found Image URL------------------------------");
					System.out.println("------------------------------" + url + "------------------------------");
					System.out.println("------------------------------" + location + "------------------------------");

					addURLToDownload(location, "", "", "", new HashMap<String, String>(),
							title + "." + filetypePart[filetypePart.length - 1]);
					return;
				}

				// No Download Button
				Element div = doc.select("div.dev-view-deviation").first();

				Element image = div.getElementsByTag("img").first();

				String source = "";
				if (image == null) {
					System.out.println(
							"------------------------------!!!ERROR on " + url + " !!!------------------------------");

					System.out.println("------------------------------!!!Cookies: " + getDACookie()
							+ "    ------------------------------");
					System.out.println(div);
					sendUpdate(STATUS.DOWNLOAD_ERRORED, "!!!ERROR!!!\n" + url);
					return;
				}

				// When it is text art (e.g. story) the only image is the avator (profile
				// picture)
				if (image.hasClass("avatar")) {
					System.out.println(
							"------------------------------No Image found, probably text art------------------------------");
					System.out.println(url);
					return;
				}

				source = image.attr("src");

				String[] parts = source.split("/v1/");

				// Image page uses scaled down version. Split at /v1/ to receive max size.
				if (parts.length > 2) {
					System.out.println(
							"------------------------------Unexpected URL Format------------------------------");
					sendUpdate(STATUS.DOWNLOAD_WARN, "Unexpected URL Format - Risky Try");
					return;
				}

				String[] tmpParts = parts[0].split("\\.");

				System.out.println("------------------------------Found Image URL------------------------------");
				System.out.println("------------------------------" + url + "------------------------------");
				System.out.println("------------------------------" + parts[0] + "------------------------------");

				addURLToDownload(new URL(parts[0]), "", "", "", new HashMap<String, String>(),
						title + "." + tmpParts[tmpParts.length - 1]);
				return;

			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(
					"------------------------------No Full Size URL for: " + url + "------------------------------");
			sendUpdate(STATUS.DOWNLOAD_ERRORED, "No image found for " + url);

			return;

		}
	}
}