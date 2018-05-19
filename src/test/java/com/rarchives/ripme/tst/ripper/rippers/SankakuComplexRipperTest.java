package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SankakuComplexRipper;

public class SankakuComplexRipperTest extends RippersTest {
    // https://github.com/RipMeApp/ripme/issues/257
    /*
    public void testSankakuChanRip() throws IOException {
        SankakuComplexRipper ripper = new SankakuComplexRipper(new URL("https://chan.sankakucomplex.com/?tags=cleavage"));
        testRipper(ripper);
    }
    public void testSankakuIdolRip() throws IOException {
        SankakuComplexRipper ripper = new SankakuComplexRipper(new URL("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29"));
        testRipper(ripper);
    }
    */
    public void testgetGID() throws IOException {
        URL url = new URL("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29");
        SankakuComplexRipper ripper = new SankakuComplexRipper(url);
        assertEquals("idol._meme_(me!me!me!)_(cosplay)", ripper.getGID(url));
    }

    public void testgetSubDomain() throws IOException {
        URL url = new URL("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29");
        SankakuComplexRipper ripper = new SankakuComplexRipper(url);
        assertEquals("idol.", ripper.getSubDomain(url));
    }
}
