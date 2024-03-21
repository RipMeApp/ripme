package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WebtoonsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class  WebtoonsRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testWebtoonsAlbum() throws IOException, URISyntaxException {
        WebtoonsRipper ripper = new WebtoonsRipper(new URI("https://www.webtoons.com/en/super-hero/unordinary/episode-103/viewer?title_no=679&episode_no=109").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testWedramabtoonsType() throws IOException, URISyntaxException {
    	WebtoonsRipper ripper = new WebtoonsRipper(new URI("http://www.webtoons.com/en/drama/lookism/ep-145/viewer?title_no=1049&episode_no=145").toURL());
    	testRipper(ripper);
    }
    @Test
    @Disabled("URL format different")
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://www.webtoons.com/en/super-hero/unordinary/episode-103/viewer?title_no=679&episode_no=109").toURL();
        WebtoonsRipper ripper = new WebtoonsRipper(url);
        Assertions.assertEquals("super-hero", ripper.getGID(url));
    }
}
