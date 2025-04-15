package com.rarchives.ripme.ripper.rippers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

/**
 *
 * @author MrPlaygon
 *
 *         NOT using Deviantart API like the old JSON ripper because it is SLOW
 *         and somehow annoying to use. Things to consider: Using the API might
 *         be less work/maintenance later because APIs do not change as
 *         frequently as HTML source code does...?
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
 *         Deactivated account:
 *
 *         https://www.deviantart.com/gingerbreadpony/gallery
 *
 *         Banned Account:
 *
 *         https://www.deviantart.com/ghostofflossenburg/gallery
 *
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

	private static final Logger logger = LogManager.getLogger(DeviantartRipper.class);

	private final String username = "5g58l4dii5lbbpc";
	private final String password = "5g5_8l4dii5lbbpc";
	private int offset = 0;
	private boolean usingCatPath = false;
	private int downloadCount = 0;
	private Map<String, String> cookies = new HashMap<String, String>();
	private DownloadThreadPool deviantartThreadPool = new DownloadThreadPool("deviantart");
	private ArrayList<String> names = new ArrayList<String>();

	List<String> allowedCookies = Arrays.asList("agegate_state", "userinfo", "auth", "auth_secure");

	private Connection conn = null;

	// Constants
	private final String referer = "https://www.deviantart.com/";
	private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";
	private final String utilsKey = "DeviantartLogin.cookies"; //for config file

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
		if (isDeactivated()) {
			throw new IOException("Account Deactivated");
		}
		login();

		// Saving connection to reuse later for following pages.
		this.conn = Http.url(urlWithParams(this.offset)).cookies(getDACookie()).referrer(this.referer)
				.userAgent(this.userAgent).connection();

		return this.conn.get();
	}

	/**
	 * Checks if the URL refers to a deactivated account using the HTTP status Codes
	 *
	 * @return true when the account is good
	 * @throws IOException when the account is deactivated
	 */
	private boolean isDeactivated() throws IOException {
		Response res = Http.url(this.url).connection().followRedirects(true).referrer(this.referer)
				.userAgent(this.userAgent).execute();
		return res.statusCode() != 200 ? true : false;

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

		String customUsername = Utils.getConfigString("DeviantartCustomLoginUsername", this.username);
		String customPassword = Utils.getConfigString("DeviantartCustomLoginPassword", this.password);
		try {
			String dACookies = Utils.getConfigString(utilsKey, null);
			updateCookie(dACookies != null ? deserialize(dACookies) : null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (getDACookie() == null || !checkLogin()) {
			logger.info("Do Login now");
			// Do login now

			Map<String, String> tmpCookies = new HashMap<String, String>();

			// Load login page
			Response res = Http.url("https://www.deviantart.com/users/login").connection().method(Method.GET)
					.referrer(referer).userAgent(userAgent).execute();

			tmpCookies.putAll(res.cookies());

			// Find tokens
			Document doc = res.parse();

			tmpCookies.putAll(res.cookies());

			Element form = doc.getElementById("login");
			String token = form.select("input[name=\"validate_token\"]").first().attr("value");
			String key = form.select("input[name=\"validate_key\"]").first().attr("value");
			logger.info("Token: " + token + " & Key: " + key);

			// Build Login Data
			HashMap<String, String> loginData = new HashMap<String, String>();
			loginData.put("challenge", "");
			loginData.put("username", customUsername);
			loginData.put("password", customPassword);
			loginData.put("remember_me", "1");
			loginData.put("validate_token", token);
			loginData.put("validate_key", key);

			// Log in using data. Handle redirect
			res = Http.url("https://www.deviantart.com/users/login").connection().referrer(referer).userAgent(userAgent)
					.method(Method.POST).data(loginData).cookies(tmpCookies).followRedirects(false).execute();

			tmpCookies.putAll(res.cookies());

			res = Http.url(res.header("location")).connection().referrer(referer).userAgent(userAgent)
					.method(Method.GET).cookies(tmpCookies).followRedirects(false).execute();

			// Store cookies
			tmpCookies.putAll(res.cookies());

			updateCookie(tmpCookies);


		} else {
			logger.info("No new Login needed");
		}

		logger.info("DA Cookies: " + getDACookie());
	}

	/**
	 * Returns next page Document using offset.
	 */
	@Override
	public Document getNextPage(Document doc) throws IOException {
		this.offset += 24;
		this.conn.url(urlWithParams(this.offset)).cookies(getDACookie());
		Response re = this.conn.execute();
		//updateCookie(re.cookies());
		Document docu = re.parse();
		Elements messages = docu.getElementsByClass("message");
		logger.info("Current Offset: " + this.offset);

		if (messages.size() > 0) {

			// if message exists -> last page
			logger.info("Messages amount: " + messages.size() + " - Next Page does not exists");
			throw new IOException("No more pages");
		}

		return Http.url(urlWithParams(this.offset)).referrer(referer).userAgent(userAgent).cookies(getDACookie()).get();
	}

	/**
	 * Returns list of Links to the Image pages. NOT links to fullsize image!!! e.g.
	 * https://www.deviantart.com/kageuri/art/RUBY-568396655
	 *
	 * @param page Page of album with multiple images
	 *
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

		logger.info("Amount of Images on Page: " + result.size());
		logger.info(page.location());

		return result;
	}

	/**
	 * Starts new Thread to find download link + filename + filetype
	 *
	 * @param url The URL to an image site.
	 */
	@Override
	protected void downloadURL(URL url, int index) {
		this.downloadCount += 1;
		logger.info("Downloading URL Number " + this.downloadCount);
		logger.info("Deviant Art URL: " + url.toExternalForm());
		try {
			// Suppress this warning because it is part of code that was temporarily
			// commented out to disable the behavior.
			// We know there's a lot about this ripper that needs to be fixed so
			// we're not too worried about warnings in this file.
			@SuppressWarnings("unused")
			Response re = Http.url(urlWithParams(this.offset)).cookies(getDACookie()).referrer(referer)
					.userAgent(userAgent).response();
			//updateCookie(re.cookies());
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
		if (s.contains("/gallery")) {
			what = "gallery";
		} else if (s.contains("/favourites")) {
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
		} else if (s.endsWith("/favourites/") || s.endsWith("/gallery/") || s.endsWith("/gallery") || s.endsWith("/favourites")) { //added andings without trailing / because of https://github.com/RipMeApp/ripme/issues/1303
			albumname = "featured";
		} else if (artistM.matches()) {
			albumname = artistM.group(1);
		}
		logger.info("Album Name: " + artist + "_" + what + "_" + albumname);

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
	 * Offset misleasing because it might say 24 but it is not the 24th image. (DA site is bugged I guess)
	 *
	 * @return URL to page with offset
	 */
	private URL urlWithParams(int offset) {
		try {
			String url = cleanURL();
			if (this.usingCatPath) {
				return (new URI(url + "?catpath=/&offset=" + offset)).toURL();
			} else {
				return (new URI(url + "?offset=" + offset).toURL());
			}
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns Hashmap usable as Cookie for NSFW Artworks. Method Not really needed but
	 * maybe useful later.
	 *
	 * @return Cookie Hashmap
	 */
	private Map<String, String> getDACookie() {
		return this.cookies;
	}

	/**
	 * Updates cookies and saves to config file.
	 *
	 * @param m new Cookies
	 */
	private void updateCookie(Map<String, String> m) {
		if (m == null) {
			return;
		}

		/*Iterator<String> iter = m.keySet().iterator();
		while (iter.hasNext()) {
			String current = iter.next();
			if (!this.allowedCookies.contains(current)) {
				iter.remove();
			}
		}*/

		logger.info("Updating Cookies");
		logger.info("Old Cookies: " + getDACookie() + " ");
		logger.info("New Cookies: " + m + " ");
		this.cookies.putAll(m);
		this.cookies.put("agegate_state", "1");
		logger.info("Merged Cookies: " + getDACookie() + " ");

		try {
			Utils.setConfigString(utilsKey, serialize(new HashMap<String, String>(getDACookie())));
			Utils.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Serializes an Object and returns a String ready to store Used to store
	 * cookies in the config file because the deviantart cookies contain all sort of
	 * special characters like ; , = : and so on.
	 *
	 * @param o Object to serialize
	 * @return The serialized base64 encoded object
	 * @throws IOException
	 */
	private String serialize(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	/**
	 * Recreates the object from the base64 encoded String. Used for Cookies
	 *
	 * @param s the Base64 encoded string
	 * @return the Cookie Map
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Map<String, String> deserialize(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

		// Suppress this warning because it's part of the legacy implementation.
		// We know there's a lot about this ripper that needs to be fixed so
		// we're not too worried about warnings in this file.
		// Unchecked cast here but should never be something else.
		@SuppressWarnings("unchecked")
		HashMap<String, String> o = (HashMap<String, String>) ois.readObject();
		ois.close();
		return o;
	}

	/**
	 * Checks if the current cookies are still valid/usable.
	 * Also checks if agegate is given.
	 *
	 *
	 * @return True when all is good.
	 */
	private boolean checkLogin() {
		if (!getDACookie().containsKey("agegate_state")) {
			logger.info("No agegate key");
			return false;
		} else if (!getDACookie().get("agegate_state").equals("1")) { // agegate == 1 -> all is fine. NSFW is visible
			logger.info("Wrong agegate value");
			return false;
		}

		try {
			logger.info("Login with Cookies: " + getDACookie());
			Response res = Http.url("https://www.deviantart.com/users/login").connection().followRedirects(true)
					.cookies(getDACookie()).referrer(this.referer).userAgent(this.userAgent).execute();
			if (!res.url().toExternalForm().equals("https://www.deviantart.com/users/login") && !res.url().toExternalForm().startsWith("https://www.deviantart.com/users/wrong-password")) {
				logger.info("Cookies are valid: " + res.url());
				return true;
			} else {
				logger.info("Cookies invalid. Wrong URL: " + res.url() + "  " + res.statusCode());
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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
	private class DeviantartImageThread implements Runnable {
		private final URL url;

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
		 * @return URL like
		 *         https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/intermediary/f/07f7a6bb-2d35-4630-93fc-be249af22b3e/d7jak0y-d20e5932-df72-4d13-b002-5e122037b373.jpg
		 *
		 *
		 */
		private void getFullSizeURL() {

			logger.info("Searching max. Resolution for " + url);
			sendUpdate(STATUS.LOADING_RESOURCE, "Searching max. resolution for " + url);
			try {
				Response re = Http.url(url).connection().referrer(referer).userAgent(userAgent).cookies(getDACookie())
						.execute();
				Document doc = re.parse();

				// Artwork Title
				String title = doc.select("a.title").first().html();
				title = title.replaceAll("[^a-zA-Z0-9\\.\\-]", "_").toLowerCase();

				int counter = 1; // For images with same name add _X (X = number)
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
					logger.info("Download Button found for "+ url +" : "  + downloadButton.attr("href"));

					Response download = Http.url(downloadButton.attr("href")).connection().cookies(getDACookie())
							.method(Method.GET).referrer(referer).userAgent(userAgent).ignoreContentType(true)
							.followRedirects(true).execute();
					URL location = download.url();

					String[] filetypePart = download.header("Content-Disposition").split("\\.");

					logger.info("Found Image URL");
					logger.info(url);
					logger.info(location);

					addURLToDownload(location, "", "", "", getDACookie(),
							title + "." + filetypePart[filetypePart.length - 1]);
					return;
				}

				// No Download Button
				logger.info("No Download Button for: "+ url);

				Element div = doc.select("div.dev-view-deviation").first();

				Element image = div.getElementsByTag("img").first();

				String scaledImage = "";
				if (image == null) {
					logger.error("ERROR on " + url);

					logger.error("Cookies: " + getDACookie() + "    ");
					logger.error(div);
					sendUpdate(STATUS.DOWNLOAD_ERRORED, "ERROR at\n" + url);
					return;
				}

				// When it is text art (e.g. story) the only image is the profile
				// picture
				if (image.hasClass("avatar")) {
					logger.error("No Image found, probably text art: " + url);
					return;
				}

				scaledImage = image.attr("src").split("\\?")[0];

				String[] parts = scaledImage.split("/v1/"); // Image page uses scaled down version. Split at /v1/ to receive max size.

				if (parts.length > 2) {
					logger.error("Unexpected URL Format");
					sendUpdate(STATUS.DOWNLOAD_ERRORED, "Unexpected URL Format");
					return;
				}

				String originalImage = parts[0]; // URL to original image without scaling (works not alwys. weird 404 errors.)
				String downloadString = originalImage; // this works always
				try {
					Http.url(downloadString).connection().cookies(getDACookie()).method(Method.GET).referrer(referer).userAgent(userAgent).ignoreContentType(true).followRedirects(true).execute().statusCode(); //Error on 404
				}catch (HttpStatusException e) {
					downloadString = scaledImage; //revert back to save url because of error
				}
				String[] tmpParts = downloadString.split("\\."); //split to get file ending

				addURLToDownload(new URI(downloadString).toURL(), "", "", "", new HashMap<String, String>(),
						title + "." + tmpParts[tmpParts.length - 1]);
				return;

			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}

			logger.error("No Full Size URL for: " + url);
			sendUpdate(STATUS.DOWNLOAD_ERRORED, "No image found for " + url);

			return;

		}
	}
}
