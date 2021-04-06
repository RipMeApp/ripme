package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.RedgifsRipper;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;

public class RedgifsRipperTest extends RippersTest {

    /**
     * Rips correctly formatted URL directly from Redgifs
     * @throws IOException
     */
    @Test
    @Disabled("test or ripper broken")
    public void testRedgifsGoodURL() throws IOException{
        RedgifsRipper ripper = new RedgifsRipper(new URL("https://www.redgifs.com/watch/talkativewarpeddragon-petite"));
        testRipper(ripper);
    }

    /**
     * Rips gifdeliverynetwork URL's by redirecting them to proper redgifs url
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedgifsBadRL() throws IOException{
        RedgifsRipper ripper = new RedgifsRipper(new URL("https://www.gifdeliverynetwork.com/foolishelasticchimpanzee"));
        testRipper(ripper);
    }

    /**
     * Rips a Redifs profile
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedgifsProfile() throws IOException {
        RedgifsRipper ripper  = new RedgifsRipper(new URL("https://redgifs.com/users/margo_monty"));
        testRipper(ripper);
    }

    /**
     * Rips a Redifs category/search
     * @throws IOException
     */
    @Test
    @Disabled("test or ripper broken")
    public void testRedgifsSearch() throws IOException {
        RedgifsRipper ripper  = new RedgifsRipper(new URL("https://redgifs.com/gifs/browse/little-caprice"));
        Document doc = ripper.getFirstPage();

        doc = ripper.getNextPage(doc);
        Assertions.assertTrue("https://napi.redgifs.com/v1/gfycats/search?search_text=little%20caprice&count=150&start=150".equalsIgnoreCase(doc.location()));
        doc = ripper.getNextPage(doc);
        Assertions.assertTrue("https://napi.redgifs.com/v1/gfycats/search?search_text=little%20caprice&count=150&start=300".equalsIgnoreCase(doc.location()));
    }
}
