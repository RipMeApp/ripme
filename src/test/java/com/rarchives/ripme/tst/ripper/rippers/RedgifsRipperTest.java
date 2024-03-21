package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.RedditRipper;
import com.rarchives.ripme.ripper.rippers.RedgifsRipper;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RedgifsRipperTest extends RippersTest {

    /**
     * Rips correctly formatted URL directly from Redgifs
     */
    @Test
    @Disabled("test or ripper broken")
    public void testRedgifsGoodURL() throws IOException, URISyntaxException {
        RedgifsRipper ripper = new RedgifsRipper(new URI("https://www.redgifs.com/watch/talkativewarpeddragon-petite").toURL());
        testRipper(ripper);
    }

    /**
     * Rips gifdeliverynetwork URL's by redirecting them to proper redgifs url
     */
    @Test
    @Tag("flaky")
    public void testRedgifsBadRL() throws IOException, URISyntaxException {
        RedgifsRipper ripper = new RedgifsRipper(new URI("https://www.gifdeliverynetwork.com/foolishelasticchimpanzee").toURL());
        testRipper(ripper);
    }

    /**
     * Rips a Redifs profile
     */
    @Test
    @Tag("flaky")
    public void testRedgifsProfile() throws IOException, URISyntaxException {
        RedgifsRipper ripper  = new RedgifsRipper(new URI("https://redgifs.com/users/margo_monty").toURL());
        testRipper(ripper);
    }

    /**
     * Rips a Redifs category/search
     * @throws IOException
     */
    @Test
    @Disabled("test or ripper broken")
    public void testRedgifsSearch() throws IOException, URISyntaxException {
        RedgifsRipper ripper  = new RedgifsRipper(new URI("https://redgifs.com/gifs/browse/little-caprice").toURL());
        Document doc = ripper.getFirstPage();

        doc = ripper.getNextPage(doc);
        Assertions.assertTrue("https://api.redgifs.com/v1/gfycats/search?search_text=little%20caprice&count=150&start=150".equalsIgnoreCase(doc.location()));
        doc = ripper.getNextPage(doc);
        Assertions.assertTrue("https://api.redgifs.com/v1/gfycats/search?search_text=little%20caprice&count=150&start=300".equalsIgnoreCase(doc.location()));
    }

    @Test
    @Tag("flaky")
    public void testRedditRedgifs() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/r/nsfwhardcore/comments/ouz5bw/me_cumming_on_his_face/").toURL());
        testRipper(ripper);
    }
}
