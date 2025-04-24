package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.HypnohubRipper;

public class HypnohubRipperTest extends RippersTest {
    private static final String POOL_URL = "https://hypnohub.net/index.php?page=pool&s=show&id=6717";
    private static final String POST_URL = "https://hypnohub.net/index.php?page=post&s=view&id=234499&pool_id=6717";

    @Test
    public void testRipPoolAndPost() throws IOException, URISyntaxException {
        URL poolURL = new URI(POOL_URL).toURL();
        HypnohubRipper poolRipper = new HypnohubRipper(poolURL);
        testRipper(poolRipper);
        URL postURL = new URI(POST_URL).toURL();
        HypnohubRipper postRipper = new HypnohubRipper(postURL);
        testRipper(postRipper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL poolURL = new URI(POOL_URL).toURL();
        HypnohubRipper poolRipper = new HypnohubRipper(poolURL);
        Assertions.assertEquals("6717", poolRipper.getGID(poolURL));

        URL postURL = new URI(POST_URL).toURL();
        HypnohubRipper postRipper = new HypnohubRipper(postURL);
        Assertions.assertEquals("post&s=view&id=234499&pool_id=6717", postRipper.getGID(postURL));
    }
}
