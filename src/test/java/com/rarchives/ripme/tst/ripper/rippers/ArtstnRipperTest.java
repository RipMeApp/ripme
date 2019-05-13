package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ArtstnRipper;

public class ArtstnRipperTest extends RippersTest {

	public void testSingleProject() throws IOException {
		URL url = new URL("https://artstn.co/p/JlE15Z");
		testRipper(new ArtstnRipper(url));
	}

	public void testUserPortfolio() throws IOException {
		URL url = new URL("https://artstn.co/m/rv37");
		testRipper(new ArtstnRipper(url));
	}
}
