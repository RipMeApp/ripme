package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.LusciousRipper;

public class LusciousRipperTest extends RippersTest {
    public void testPahealRipper() throws IOException {
        // a photo set
        LusciousRipper ripper = new LusciousRipper(new URL("https://luscious.net/albums/h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609/"));
        testRipper(ripper);
    }
}