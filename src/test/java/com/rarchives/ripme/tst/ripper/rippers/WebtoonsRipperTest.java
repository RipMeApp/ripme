package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WebtoonsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class WebtoonsRipperTest extends RippersTest {
    @Test
    public void testWebtoonsAlbum() throws IOException {
        WebtoonsRipper ripper = new WebtoonsRipper(new URL("https://www.webtoons.com/en/super-hero/unordinary/episode-103/viewer?title_no=679&episode_no=109"));
        testRipper(ripper);
    }
    @Test
    public void testWedramabtoonsType() throws IOException {
    	WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://www.webtoons.com/en/drama/lookism/ep-145/viewer?title_no=1049&episode_no=145"));
    	testRipper(ripper);
    }
    @Test
    @Disabled("URL format different")
    public void testGetGID() throws IOException {
        URL url = new URL("https://www.webtoons.com/en/super-hero/unordinary/episode-103/viewer?title_no=679&episode_no=109");
        WebtoonsRipper ripper = new WebtoonsRipper(url);
        Assertions.assertEquals("super-hero", ripper.getGID(url));
    }
}
