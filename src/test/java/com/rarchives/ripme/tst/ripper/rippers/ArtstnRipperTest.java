package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ArtstnRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ArtstnRipperTest extends RippersTest {
	@Test
	@Tag("flaky")
	public void testSingleProject() throws IOException, URISyntaxException {
		URL url = new URI("https://artstn.co/p/JlE15Z").toURL();
		testRipper(new ArtstnRipper(url));
	}

	@Test
	@Disabled("Failed with cloudflare protection")
	public void testUserPortfolio() throws IOException, URISyntaxException {
		URL url = new URI("https://artstn.co/m/rv37").toURL();
		testRipper(new ArtstnRipper(url));
	}
}
