package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.YuvutuRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class YuvutuRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testYuvutuAlbum1() throws IOException, URISyntaxException {
        YuvutuRipper ripper = new YuvutuRipper(new URI("http://www.yuvutu.com/modules.php?name=YuGallery&action=view&set_id=127013").toURL());
        testRipper(ripper);
    }
    @Test
    public void testYuvutuAlbum2() throws IOException, URISyntaxException {
        YuvutuRipper ripper = new YuvutuRipper(new URI("http://www.yuvutu.com/modules.php?name=YuGallery&action=view&set_id=420333").toURL());
        testRipper(ripper);
    }

    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://www.yuvutu.com/modules.php?name=YuGallery&action=view&set_id=420333").toURL();
        YuvutuRipper ripper = new YuvutuRipper(url);
        Assertions.assertEquals("420333", ripper.getGID(url));
    }
}
