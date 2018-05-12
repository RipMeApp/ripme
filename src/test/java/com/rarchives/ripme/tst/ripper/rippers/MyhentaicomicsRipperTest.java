package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MyhentaicomicsRipper;

public class MyhentaicomicsRipperTest extends RippersTest {
    public void testMyhentaicomicsAlbum() throws IOException {
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(new URL("http://myhentaicomics.com/index.php/Nienna-Lost-Tales"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://myhentaicomics.com/index.php/Nienna-Lost-Tales");
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(url);
        // Test a comic
        assertEquals("Nienna-Lost-Tales", ripper.getGID(url));
        // Test a search
        assertEquals("test", ripper.getGID(new URL("http://myhentaicomics.com/index.php/search?q=test")));
        // Test a tag
        assertEquals("2409", ripper.getGID(new URL("http://myhentaicomics.com/index.php/tag/2409/")));
    }
}