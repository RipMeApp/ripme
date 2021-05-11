package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PornhubRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class PornhubRipperTest extends RippersTest {
    @Test
    public void testPornhubRip() throws IOException {
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
            PornhubRipper ripper = new PornhubRipper(new URL("https://www.pornhub.com/album/15680522"));
            testRipper(ripper);
        }
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://www.pornhub.com/album/15680522?page=2");
        PornhubRipper ripper = new PornhubRipper(url);
        Assertions.assertEquals("15680522", ripper.getGID(url));
        url = new URL("https://www.pornhub.com/album/15680522");
        Assertions.assertEquals("15680522", ripper.getGID(url));
    }

    @Test
    @Tag("flaky")
    public void testGetNextPage() throws IOException {
        String baseURL = "https://www.pornhub.com/album/30687901";
        PornhubRipper ripper = new PornhubRipper(new URL(baseURL));
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
