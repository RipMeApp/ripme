package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MyhentaicomicsRipper;

public class MyhentaicomicsRipperTest extends RippersTest {
    public void testMyhentaicomicsAlbum() throws IOException {
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(new URL("http://myhentaicomics.com/index.php/Nienna-Lost-Tales"));
        testRipper(ripper);
    }
}