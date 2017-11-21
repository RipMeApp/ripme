package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EightmusesRipper;

public class EightmusesRipperTest extends RippersTest {
    public void testEightmusesAlbum() throws IOException {
        EightmusesRipper ripper = new EightmusesRipper(new URL("https://www.8muses.com/album/jab-comics/a-model-life"));
        testRipper(ripper);
    }
}
