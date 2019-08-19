package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EromeRipper;
import org.junit.jupiter.api.Test;

public class EromeRipperTest extends RippersTest {
	@Test
	public void testGetGIDProfilePage() throws IOException {
		URL url = new URL("https://www.erome.com/Jay-Jenna");
		EromeRipper ripper = new EromeRipper(url);
		assertEquals("Jay-Jenna", ripper.getGID(url));
	}
	@Test
	public void testGetGIDAlbum() throws IOException {
		URL url = new URL("https://www.erome.com/a/KbDAM1XT");
		EromeRipper ripper = new EromeRipper(url);
		assertEquals("KbDAM1XT", ripper.getGID(url));
	}
	@Test
	public void testGetAlbumsToQueue() throws IOException {
		URL url = new URL("https://www.erome.com/Jay-Jenna");
		EromeRipper ripper = new EromeRipper(url);
		assert (2 >= ripper.getAlbumsToQueue(ripper.getFirstPage()).size());
	}
	@Test
	public void testPageContainsAlbums() throws IOException {
		URL url = new URL("https://www.erome.com/Jay-Jenna");
		EromeRipper ripper = new EromeRipper(url);
		assert (ripper.pageContainsAlbums(url));
		assert (!ripper.pageContainsAlbums(new URL("https://www.erome.com/a/KbDAM1XT")));
	}

	public void testRip() throws IOException {
		URL url = new URL("https://www.erome.com/a/vlefBdsg");
		EromeRipper ripper = new EromeRipper(url);
		testRipper(ripper);
	}
	@Test
	public void testGetURLsFromPage() throws IOException {
		URL url = new URL("https://www.erome.com/a/Tak8F2h6");
		EromeRipper ripper = new EromeRipper(url);
		assert (35 == ripper.getURLsFromPage(ripper.getFirstPage()).size());
	}
}
