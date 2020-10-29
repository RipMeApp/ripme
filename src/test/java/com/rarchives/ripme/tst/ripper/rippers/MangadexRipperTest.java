package com.rarchives.ripme.tst.ripper.rippers;


import com.rarchives.ripme.ripper.rippers.MangadexRipper;

import java.io.IOException;
import java.net.URL;

public class MangadexRipperTest extends RippersTest{
    public void testRip() throws IOException {
        MangadexRipper ripper = new MangadexRipper(new URL("https://mangadex.org/chapter/467904/"));
        testRipper(ripper);
    }
    public class testMangaRip extends RippersTest{

        public void testRip() throws IOException {
            MangadexRipper ripper = new MangadexRipper(new URL("https://mangadex.org/title/44625/this-croc-will-die-in-100-days"));
            testRipper(ripper);
        }
    }

}
