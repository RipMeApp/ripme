package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ArtstnRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ArtstnRipperTest extends RippersTest {
	@Test
	@Tag("flaky")
	public void testSingleProject() throws IOException {
		URL url = new URL("https://artstn.co/p/JlE15Z");
		testRipper(new ArtstnRipper(url));
	}

	@Test
	@Disabled("Failed with cloudflare protection")
	public void testUserPortfolio() throws IOException {
		URL url = new URL("https://artstn.co/m/rv37");
		testRipper(new ArtstnRipper(url));
	}
}
