package com.rarchives.ripme.tst.ripper.rippers;


import com.rarchives.ripme.ripper.rippers.MangadexRipper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MangadexRipperTest extends RippersTest{
    public void testRip() throws IOException, URISyntaxException {
        MangadexRipper ripper = new MangadexRipper(new URI("https://mangadex.org/chapter/467904/").toURL());
        testRipper(ripper);
    }
    public class testMangaRip extends RippersTest{

        public void testRip() throws IOException, URISyntaxException {
            MangadexRipper ripper = new MangadexRipper(new URI("https://mangadex.org/title/44625/this-croc-will-die-in-100-days").toURL());
            testRipper(ripper);
        }
    }

}
