package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NewsfilterRipper;

public class NewsfilterRipperTest extends RippersTest {

    public void testNewsfilterRip() throws IOException {
        NewsfilterRipper ripper = new NewsfilterRipper(new URL("http://newsfilter.org/gallery/he-doubted-she-would-fuck-on-cam-happy-to-be-proven-wrong-216799"));
        testRipper(ripper);
    }
}