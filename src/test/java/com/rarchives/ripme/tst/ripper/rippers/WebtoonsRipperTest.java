package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WebtoonsRipper;
import org.junit.jupiter.api.Test;

public class WebtoonsRipperTest extends RippersTest {
    @Test
    public void testWebtoonsAlbum() throws IOException {
        WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://www.webtoons.com/en/drama/my-boo/ep-33/viewer?title_no=1185&episode_no=33"));
        testRipper(ripper);
    }
    @Test
    public void testWebtoonsType() throws IOException {
    	WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://www.webtoons.com/en/drama/lookism/ep-145/viewer?title_no=1049&episode_no=145"));
    	testRipper(ripper);
    }
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://www.webtoons.com/en/drama/my-boo/ep-33/viewer?title_no=1185&episode_no=33");
        WebtoonsRipper ripper = new WebtoonsRipper(url);
        assertEquals("my-boo", ripper.getGID(url));
    }
}
