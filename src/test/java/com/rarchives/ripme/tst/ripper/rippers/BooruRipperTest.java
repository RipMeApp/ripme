package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.BooruRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BooruRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        List<URL> passURLs = new ArrayList<>();
        passURLs.add(new URL("https://xbooru.com/index.php?page=post&s=list&tags=furry"));
        passURLs.add(new URL("https://gelbooru.com/index.php?page=post&s=list&tags=animal_ears"));

        for (URL url : passURLs) {
            BooruRipper ripper = new BooruRipper(url);
            testRipper(ripper);
        }
    }

    @Test
    public void testGetGID() throws IOException {
        URL xbooruUrl = new URL("https://xbooru.com/index.php?page=post&s=list&tags=furry");
        URL gelbooruUrl = new URL("https://gelbooru.com/index.php?page=post&s=list&tags=animal_ears");

        BooruRipper xbooruRipper = new BooruRipper(xbooruUrl);
        BooruRipper gelbooruRipper = new BooruRipper(gelbooruUrl);

        Assertions.assertEquals("furry", xbooruRipper.getGID(xbooruUrl));
        Assertions.assertEquals("animal_ears", gelbooruRipper.getGID(gelbooruUrl));
    }

    @Test
    public void testGetDomain() throws IOException {
        URL xbooruUrl = new URL("https://xbooru.com/index.php?page=post&s=list&tags=furry");
        URL gelbooruUrl = new URL("https://gelbooru.com/index.php?page=post&s=list&tags=animal_ears");

        BooruRipper xbooruRipper = new BooruRipper(xbooruUrl);
        BooruRipper gelbooruRipper = new BooruRipper(gelbooruUrl);

        Assertions.assertEquals("xbooru.com", xbooruRipper.getDomain());
        Assertions.assertEquals("gelbooru.com", gelbooruRipper.getDomain());
    }

    @Test
    public void testGetHost() throws IOException {
        URL xbooruUrl = new URL("https://xbooru.com/index.php?page=post&s=list&tags=furry");
        URL gelbooruUrl = new URL("https://gelbooru.com/index.php?page=post&s=list&tags=animal_ears");

        BooruRipper xbooruRipper = new BooruRipper(xbooruUrl);
        BooruRipper gelbooruRipper = new BooruRipper(gelbooruUrl);

        Assertions.assertEquals("xbooru", xbooruRipper.getHost());
        Assertions.assertEquals("gelbooru", gelbooruRipper.getHost());
    }
}