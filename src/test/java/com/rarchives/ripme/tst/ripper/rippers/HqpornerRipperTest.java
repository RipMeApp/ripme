package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.HqpornerRipper;
import com.rarchives.ripme.utils.Utils;

import java.io.IOException;
import java.net.URL;

public class HqpornerRipperTest extends RippersTest {

	public void testRip() throws IOException {
		if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
			HqpornerRipper ripper = new HqpornerRipper(
					new URL("https://hqporner.com/hdporn/84636-pool_lesson_with_a_cheating_husband.html"));
			testRipper(ripper);
		}
	}

	public void testGetGID() throws IOException {
		URL poolURL = new URL("https://hqporner.com/hdporn/84636-pool_lesson_with_a_cheating_husband.html");
		HqpornerRipper ripper = new HqpornerRipper(poolURL);
		assertEquals("84636-pool_lesson_with_a_cheating_husband", ripper.getGID(poolURL));
	}

	public void testGetURLsFromPage() throws IOException {
		URL actressUrl = new URL("https://hqporner.com/actress/kali-roses");
		HqpornerRipper ripper = new HqpornerRipper(actressUrl);
		assert (ripper.getURLsFromPage(ripper.getFirstPage()).size() >= 2);
	}

	public void testGetNextPage() throws IOException {
		URL multiPageUrl = new URL("https://hqporner.com/category/tattooed");
		HqpornerRipper multiPageRipper = new HqpornerRipper(multiPageUrl);
		assert (multiPageRipper.getNextPage(multiPageRipper.getFirstPage()) != null);

		URL singlePageUrl = new URL("https://hqporner.com/actress/amy-reid");
		HqpornerRipper ripper = new HqpornerRipper(singlePageUrl);
		try {
			ripper.getNextPage(ripper.getFirstPage());
		} catch (IOException e) {
			assertEquals(e.getMessage(), "No next page found.");
		}
	}

	//	public void testDifferentVideoHost() throws IOException {
	//		URL myDaddyUrl = new URL("https://hqporner.com/hdporn/90598-gangbang_with_Kali_Roses.html");
	//		HqpornerRipper myDaddyRipper = new HqpornerRipper(myDaddyUrl);
	//		testRipper(myDaddyRipper);
	//
	//		URL flyFlvUrl = new URL(
	//				"https://hqporner.com/hdporn/69862-bangbros_-_amy_reid_taking_off_a_tight_sexy_swimsuit.html");
	//		HqpornerRipper flyFlvRipper = new HqpornerRipper(flyFlvUrl);
	//		testRipper(flyFlvRipper);
	//
	//		URL unknownHostUrl = new URL("https://hqporner.com/hdporn/79528-Kayden_Kross_-_Serious_Masturbation.html"); // howq.cc
	//		HqpornerRipper unknownHostRipper = new HqpornerRipper(unknownHostUrl);
	//		testRipper(unknownHostRipper);
	//	}
}