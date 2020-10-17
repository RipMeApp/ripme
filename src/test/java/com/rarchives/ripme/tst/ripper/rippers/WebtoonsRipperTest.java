package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WebtoonsRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class WebtoonsRipperTest extends RippersTest {
    @Test
    public void testWebtoonsAlbum() throws IOException {
<<<<<<< HEAD
        WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://www.webtoons.com/en/drama/my-boo/ep-33/viewer?title_no=1185&episode_no=33"));
        testRipper(ripper);
    }
    @Test
    public void testWebtoonsType() throws IOException {
=======
        WebtoonsRipper ripper = new WebtoonsRipper(new URL("https://www.webtoons.com/en/super-hero/unordinary/episode-103/viewer?title_no=679&episode_no=109"));
        testRipper(ripper);
    }
    @Test
    public void testWedramabtoonsType() throws IOException {
>>>>>>> upstream/master
    	WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://www.webtoons.com/en/drama/lookism/ep-145/viewer?title_no=1049&episode_no=145"));
    	testRipper(ripper);
    }
    @Test
<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("http://www.webtoons.com/en/drama/my-boo/ep-33/viewer?title_no=1185&episode_no=33");
        WebtoonsRipper ripper = new WebtoonsRipper(url);
        assertEquals("my-boo", ripper.getGID(url));
=======
    @Disabled("URL format different")
    public void testGetGID() throws IOException {
        URL url = new URL("https://www.webtoons.com/en/super-hero/unordinary/episode-103/viewer?title_no=679&episode_no=109");
        WebtoonsRipper ripper = new WebtoonsRipper(url);
        Assertions.assertEquals("super-hero", ripper.getGID(url));
>>>>>>> upstream/master
    }
}
