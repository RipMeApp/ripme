package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.PornhubRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class PornhubRipperTest extends RippersTest {
    @Test
    public void testPornhubAlbumRip() throws IOException, URISyntaxException {
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
            PornhubRipper ripper = new PornhubRipper(new URI("https://www.pornhub.com/album/6299702").toURL());
            testRipper(ripper);
        }
    }

    @Test
    public void testPornhubMultiPageAlbumRip() throws IOException, URISyntaxException {
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
            PornhubRipper ripper = new PornhubRipper(new URI("https://www.pornhub.com/album/39341891").toURL());
            testRipper(ripper);
        }
    }

    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://www.pornhub.com/album/15680522?page=2").toURL();
        PornhubRipper ripper = new PornhubRipper(url);
        Assertions.assertEquals("15680522", ripper.getGID(url));
        url = new URI("https://www.pornhub.com/album/15680522").toURL();
        Assertions.assertEquals("15680522", ripper.getGID(url));
    }

    @Test
    @Tag("flaky")
    public void testGetNextPage() throws IOException, URISyntaxException {
        String baseURL = "https://www.pornhub.com/album/39341891";
        PornhubRipper ripper = new PornhubRipper(new URI(baseURL).toURL());
        Document page = Http.url(baseURL).get();
        int numPagesRemaining = 1;
        for (int idx = 0; idx < numPagesRemaining; idx++){
            page = ripper.getNextPage(page);
            Assertions.assertEquals(baseURL + "?page=" + (idx + 2), page.location());
        }
        try {
            page = ripper.getNextPage(page);
            Assertions.fail("Get next page did not throw an exception on the last page");
        } catch(IOException e){
            Assertions.assertEquals(e.getMessage(), "No more pages");
        }
    }
}
