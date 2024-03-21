package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jsoup.Connection.Response;

import com.rarchives.ripme.utils.Http;

/*
 * Ripper for ArtStation's short URL domain.
 * Example URL: https://artstn.co/p/JlE15Z
 */

public class ArtstnRipper extends ArtStationRipper {
	public URL artStationUrl = null;

	public ArtstnRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public boolean canRip(URL url) {
		return url.getHost().endsWith("artstn.co");
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		if (artStationUrl == null) {
			// Run only once.
			try {
				artStationUrl = getFinalUrl(url);
				if (artStationUrl == null) {
					throw new IOException("Null url received.");
				}
			} catch (IOException | URISyntaxException e) {
				LOGGER.error("Couldnt resolve URL.", e);
			}

		}
		return super.getGID(artStationUrl);
	}

	public URL getFinalUrl(URL url) throws IOException, URISyntaxException {
		if (url.getHost().endsWith("artstation.com")) {
			return url;
		}

		LOGGER.info("Checking url: " + url);
		Response response = Http.url(url).connection().followRedirects(false).execute();
		if (response.statusCode() / 100 == 3 && response.hasHeader("location")) {
			return getFinalUrl(new URI(response.header("location")).toURL());
		} else {
			return null;
		}
	}
}
