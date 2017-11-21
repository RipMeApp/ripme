package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuraffinityRipper;

public class FuraffinityRipperTest extends RippersTest {
    public void testFuraffinityAlbum() throws IOException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URL("https://www.furaffinity.net/gallery/mustardgas/"));
        testRipper(ripper);
    }
}
