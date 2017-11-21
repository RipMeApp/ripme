package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NfsfwRipper;

public class NfsfwRipperTest extends RippersTest {
    public void testNfsfwRip() throws IOException {
        NfsfwRipper ripper = new NfsfwRipper(new URL("http://nfsfw.com/gallery/v/Kitten/"));
        testRipper(ripper);
    }
}
