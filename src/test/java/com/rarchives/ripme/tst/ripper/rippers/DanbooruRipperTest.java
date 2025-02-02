package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.DanbooruRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DanbooruRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        List<URL> passURLs = new ArrayList<>();
        passURLs.add(new URI("https://danbooru.donmai.us/posts?tags=brown_necktie").toURL());
        passURLs.add(new URI("https://danbooru.donmai.us/posts?page=1&tags=pink_sweater_vest").toURL());

        for (URL url : passURLs) {
            DanbooruRipper danbooruRipper = new DanbooruRipper(url);
            testRipper(danbooruRipper);
        }
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL danBooruUrl = new URI("https://danbooru.donmai.us/posts?tags=brown_necktie").toURL();
        URL danBooruUrl2 = new URI("https://danbooru.donmai.us/posts?page=1&tags=pink_sweater_vest").toURL();

        DanbooruRipper danbooruRipper = new DanbooruRipper(danBooruUrl);
        DanbooruRipper danbooruRipper2 = new DanbooruRipper(danBooruUrl2);

        Assertions.assertEquals("brown_necktie", danbooruRipper.getGID(danBooruUrl));
        Assertions.assertEquals("pink_sweater_vest", danbooruRipper2.getGID(danBooruUrl2));
    }

    @Test
    public void testGetHost() throws IOException, URISyntaxException {
        URL danBooruUrl = new URI("https://danbooru.donmai.us/posts?tags=brown_necktie").toURL();

        DanbooruRipper danbooruRipper = new DanbooruRipper(danBooruUrl);

        Assertions.assertEquals("danbooru", danbooruRipper.getHost());
    }
}
