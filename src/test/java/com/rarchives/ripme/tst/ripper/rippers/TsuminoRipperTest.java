package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TsuminoRipper;

public class TsuminoRipperTest extends RippersTest {
    public void testPahealRipper() throws IOException {
        // a photo set
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/42882/chaldea-maid-"));
        testRipper(ripper);
    }
}