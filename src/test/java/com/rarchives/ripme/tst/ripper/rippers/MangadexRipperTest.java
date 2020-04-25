package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.MangadexRipper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class MangadexRipperTest extends RippersTest{
    @Test
    public void testRip() throws IOException {
        MangadexRipper ripper = new MangadexRipper(new URL("https://mangadex.org/chapter/467904/"));
        testRipper(ripper);
    }

    @Test
    public void testAltUrlRip() throws IOException {
        MangadexRipper ripper = new MangadexRipper(new URL("https://www.mangadex.org/chapter/804465/1"));
        testRipper(ripper);
    }
}
