package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.Hentai2readRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class Hentai2readRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testHentai2readAlbum() throws IOException {
        Hentai2readRipper ripper = new Hentai2readRipper(new URL("https://hentai2read.com/sm_school_memorial/1/"));
        testRipper(ripper);
    }
}